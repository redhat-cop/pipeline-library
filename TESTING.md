# Testing
## Automated
Currently, there are no automated tests. The plan would be to automate the manual steps via travis.

## Manual
The below steps can be used to test each pipeline lib method. If you are testing a PR or your local branch, you need to change the following:
- test/create-pipeline-library.groovy - to point to your git repo, instead of https://github.com/redhat-cop/pipeline-library.git
- test/Jenkinsfile-* - the branch for the Jenkinsfile needs to match the branch of the changes

For convenience, the below commands can automate the above. NOTE: the below presumes the remote url is https based.
```bash
sed -i "s|https://github.com/redhat-cop/pipeline-library.git|$(git config --get remote.origin.url)|g" test/create-pipeline-library.groovy
find test -type f -name "Jenkinsfile*" -exec sed -i "s/pipeline-library@master/pipeline-library@$(git rev-parse --abbrev-ref HEAD)/g" {} \;
```

### Create projects
```bash
oc new-project pipelinelib-testing
```

### Import additional image streams for slaves
```bash
oc import-image jenkins-slave-ansible --from=quay.io/redhat-cop/jenkins-slave-ansible:v1.15 --confirm
oc label imagestream jenkins-slave-ansible role=jenkins-slave

oc import-image jenkins-slave-image-mgmt --from=siamaksade/jenkins-slave-skopeo-centos7 --confirm
oc label imagestream jenkins-slave-image-mgmt role=jenkins-slave
```

// NOTE: The below image is not released into quay yet, thats why we are using the above
// oc import-image jenkins-slave-image-mgmt --from=quay.io/redhat-cop/jenkins-slave-image-mgmt --confirm
// oc label imagestream jenkins-slave-image-mgmt role=jenkins-slave

### Create BuildConfigs
```bash
find test -type f -name "Jenkinsfile-*" -exec bash -c '\
    oc process -f https://raw.githubusercontent.com/redhat-cop/openshift-templates/v1.4.9/jenkins-pipelines/jenkins-pipeline-template-no-ocp-triggers.yml \
    -p NAME=$(basename {} | tr 'A-Z' 'a-z') \
    -p PIPELINE_FILENAME=$(basename {}) \
    -p PIPELINE_CONTEXT_DIR=test \
    -p PIPELINE_SOURCE_REPOSITORY_URL=$(git config --get remote.origin.url) \
    -p PIPELINE_SOURCE_REPOSITORY_REF=$(git rev-parse --abbrev-ref HEAD)' \; | oc apply -f -
```

### Deploy Jenkins
```bash
oc process -p MEMORY_REQUEST=2Gi -p MEMORY_LIMIT=3Gi -f https://raw.githubusercontent.com/redhat-cop/openshift-templates/v1.4.9/jenkins/jenkins-persistent-template.yml | oc apply -f -
oc patch dc jenkins -p '{"apiVersion":"apps.openshift.io/v1","kind":"DeploymentConfig","metadata":{"name":"jenkins"},"spec":{"template":{"spec":{"containers":[{"name":"jenkins","resources":{"limits":{"cpu":"3"},"requests":{"cpu":"2"}}}]}}}}'
oc rollout status dc/jenkins --watch=true
```

### Create global pipeline lib
```bash
curl --header "Authorization: Bearer $(oc whoami --show-token)" --data-urlencode "script=$(< test/create-pipeline-library.groovy)" https://$(oc get route jenkins -o jsonpath={.spec.host})/scriptText
```

### Install sonar plugin
```bash
curl -X POST -d "<jenkins><install plugin='sonar@2.9' /></jenkins>" --header "Authorization: Bearer $(oc whoami --show-token)" --header "Content-Type: text/xml" https://$(oc get route jenkins -o jsonpath={.spec.host})/pluginManager/installNecessaryPlugins
```

### Create username/password and dockercfg secrets
```bash
oc create secret generic my-token --from-literal=username=openshift1 --from-literal=password=$(oc whoami --show-token)
oc label secret my-token credential.sync.jenkins.openshift.io=true

oc create secret generic --from-literal=registry=$(oc get is jenkins -n openshift -o jsonpath={.status.dockerImageRepository} | cut -d '/' -f1 | xargs) --from-literal=username=openshift1 --from-literal=token=$(oc whoami --show-token) local-registry-generic
oc secrets link --for=mount jenkins local-registry-generic

oc create secret docker-registry --docker-server=$(oc get is jenkins -n openshift -o jsonpath={.status.dockerImageRepository} | cut -d '/' -f1 | xargs) --docker-username=openshift1 --docker-password=$(oc whoami --show-token) --docker-email=unused local-registry
oc secrets link --for=mount jenkins local-registry
```

### Start all BuildConfigs
```bash
find test -type f -name "Jenkinsfile-*" -exec bash -c 'oc start-build $(basename {} | tr 'A-Z' 'a-z')-pipeline' \;
```

### Cleanup
```bash
oc delete project pipelinelib-testing pipelinelib-promotion-testing build-s2i-executable sonarqube
```