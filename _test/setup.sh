#!/bin/bash
trap "exit 1" TERM
export TOP_PID=$$
NAMESPACE="${2:-pipelinelib-testing}"
CI_REPO_SLUG="${3:-redhat-cop/pipeline-library}"
CI_BRANCH="${4:-master}"
CLONE_DIR="/tmp/${CI_REPO_SLUG}/${CI_BRANCH}"

clone() {
  rm -rf ${CLONE_DIR}
  git clone "https://github.com/${CI_REPO_SLUG}.git" ${CLONE_DIR}
  git checkout ${CI_BRANCH}
}

applier() {
  pushd ${CLONE_DIR}

  echo "${CI_BRANCH}"
  echo "${CI_REPO_SLUG}"

  sed -i "7s|.*|- src: https://github.com/${CI_REPO_SLUG}.git|" galaxy-requirements.yml
  sed -i "9s/.*/  version: ${CI_BRANCH}/g" galaxy-requirements.yml

  ansible-galaxy install -r requirements.yml -p galaxy --force
  ansible-playbook -i .applier/ galaxy/openshift-applier/playbooks/openshift-cluster-seed.yml \
    -e namespace=${NAMESPACE} \
    -e repo_ref=${CI_BRANCH} \
    -e repository_url=https://github.com/${CI_REPO_SLUG}.git \
    -e clone_dir=${CLONE_DIR} \
    -e oc_token="$(oc whoami --show-token)" \
    -e internal_registry_url="$(oc get is jenkins -n openshift -o jsonpath={.status.dockerImageRepository})"

  popd
}

test() {
  # Make sure we're logged in, and we've found at least one build to test.
  oc status > /dev/null || echo "Please log in before running tests." || exit 1
  if [ $(oc get bc -l type=pipeline -n ${NAMESPACE} --no-headers | grep -c .) -lt 1 ]; then
    echo "Did not find any builds, make sure you've passed the proper arguments."
    exit 1
  fi

  echo "Ensure all Builds are executed..."
  for pipeline in $(oc get bc -l type=pipeline -n ${NAMESPACE} -o jsonpath='{.items[*].metadata.name}'); do
    oc start-build ${pipeline} -n ${NAMESPACE}
  done

  echo "Waiting for all builds to start..."
  while [[ "$(get_build_phases "New")" -ne 0 || $(get_build_phases "Pending") -ne 0 ]]; do
    echo -ne "New Builds: $(get_build_phases "New"), Pending Builds: $(get_build_phases "Pending")$([ "$TRAVIS" != "true" ] && echo "\r" || echo "\n")"
    sleep 1
  done

  echo "Waiting for all builds to complete..."
  while [ $(get_build_phases "Running") -ne 0 ]; do
    echo -ne "Running Builds: $(get_build_phases "Running")$([ "$TRAVIS" != "true" ] && echo "\r" || echo "\n")"
    sleep 1
  done

  download_jenkins_logs_for_failed "$(ls --ignore=*fail* ${CLONE_DIR}/test/ | grep "Jenkinsfile" | tr 'A-Z' 'a-z' | xargs)" "Complete"
  download_jenkins_logs_for_failed "$(ls ${CLONE_DIR}/test/Jenkinsfile-*-fail-* | xargs -n 1 basename | tr 'A-Z' 'a-z' | xargs)" "Failed"

  logcount=$(ls *.log | wc -l)
  if [[ $logcount -gt 0 ]]; then
    echo "Tests Completed Unsuccessfully. See logs above."
    exit 1
  else
    echo "Tests Completed Successfully!"
  fi
}

get_build_phases() {
  phase=$1
  result=$(retry 5 oc get builds -l type=pipeline -o jsonpath="{.items[?(@.status.phase==\"${phase}\")].metadata.name}" -n $NAMESPACE) || kill -s TERM $TOP_PID
  echo ${result} | wc -w
}

get_build_phase_for() {
  name=$1
  result=$(retry 5 oc get builds ${name} -o jsonpath="{.status.phase}" -n $NAMESPACE) || kill -s TERM $TOP_PID
  echo ${result}
}

get_buildnumber_for() {
  name=$1
  result=$(retry 5 oc get buildconfigs ${name} -o jsonpath="{.status.lastVersion}" -n $NAMESPACE) || kill -s TERM $TOP_PID
  echo ${result}
}

download_jenkins_logs_for_failed() {
  jobs=$1
  expectedphase=$2

  echo "Checking jobs which should have an expected phase of ${expectedphase}..."

  jenkins_url=$(oc get route jenkins -n ${NAMESPACE} -o jsonpath='{ .spec.host }')
  token=$(oc whoami --show-token)

  for pipeline in ${jobs}; do
    build_number=$(get_buildnumber_for ${pipeline})
    build="${pipeline}-${build_number}"

    phase=$(get_build_phase_for ${build})
    if [[ "${expectedphase}" != "${phase}" ]]; then
      echo ""
      echo "Downloading Jenkins logs for ${build} as phase (${phase}) does not match expected (${expectedphase})..."
      curl -k -sS -H "Authorization: Bearer ${token}" "https://${jenkins_url}/blue/rest/organizations/jenkins/pipelines/${NAMESPACE}/pipelines/${NAMESPACE}-${pipeline}/runs/${build_number}/log/?start=0&download=true" -o "${pipeline}.log"

      echo "## START LOGS: ${build}"
      cat "${pipeline}.log"
      echo "## END LOGS: ${build}"
    fi
  done
}

function retry {
  local retries=$1
  shift

  local count=0
  until "$@"; do
    exit=$?
    wait=$((2 ** $count))
    count=$(($count + 1))
    if [ $count -lt $retries ]; then
      echo "Retry $count/$retries exited $exit, retrying in $wait seconds..."
      sleep $wait
    else
      echo "Retry $count/$retries exited $exit, no more retries left."
      return $exit
    fi
  done
  return 0
}

# Process arguments
case $1 in
  applier)
    clone
    applier
    ;;
  test)
    test
    ;;
  *)
    echo "Not an option"
    exit 1
esac
