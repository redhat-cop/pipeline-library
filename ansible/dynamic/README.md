# Ansible provisioning Pipeline

This Jenkins pipeline is designed to run under the following conditions:

- the Jenkins pipeline job is created via the OpenShift sync plugin, which in turn requires a `BuildConfig` with strategy `pipeline` 
- the application will be built & packaged in a [Jenkins slave pod](https://github.com/rht-labs/labs-ci-cd/tree/master/docker/jenkins-slave-ansible), not via S2I source
- the no container image will be built, but an openshift project will be set up via an infrastructure-as-code repository using the [openshift-applier](https://github.com/redhat-cop/casl-ansible/tree/master/roles/openshift-applier)


## Support Resources

The following resources are commonly used in support of this pipeline:

- [an OpenShift template](https://github.com/rht-labs/labs-ci-cd/blob/master/templates/jenkins-slave-pod/template.json) for a custom Jenkins slave pod
- Docker builds that define a [Ansible Jenkins Slave](https://github.com/rht-labs/labs-ci-cd/tree/master/docker/jenkins-slave-ansible)
- A generic application `DeploymentConfig` for apps exposes http endpoints
- The ansible automation in the [openshift-applier](https://github.com/redhat-cop/casl-ansible/tree/master/roles/openshift-applier) role in `casl-ansible`.

## Getting Started

1. Copy the `Jenkinsfile` into the root directory of your application source code
2. Review the environment variables specified in the first `node`. This part of the script will run on a Jenkins master,
and set all the needed variables for the rest of the script. There are some conventions built into the pipeline with helper methods. The comments should make this clear.
3. Commit and push your changes
4. Ensure you a pipeline `BuildConfig` in the OCP project where your Jenkins should live
5. Run the Jenkins job 