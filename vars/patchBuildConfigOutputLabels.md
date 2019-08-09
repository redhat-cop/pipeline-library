# patchBuildConfigOutputLabels

Patches the build config to add jenkins and git build information.

Parameters:

patchBuildConfigOutputLabels(bcName: String (required), domainPrefix: String (optional, default: com.redhat))

Sample usage:

Intended to run before the container build
```
stage('Container Build'){
  steps {
    patchBuildConfigOutputLabels(bcName: 'my-app', domainPrefix: 'org.example')
    script {
      openshift.withCluster () {
        openshift.startBuild( "${APP_NAME} --from-dir=${SOURCE_CONTEXT_DIR} -w" )
      }
    }
  }
}
```

Include this library by adding this before "pipeline" in your Jenkins:
```
library identifier: "pipeline-library@master", retriever: modernSCM(
  [$class: "GitSCMSource",
   remote: "https://github.com/redhat-cop/pipeline-library.git"])
