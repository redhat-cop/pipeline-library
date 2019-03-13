# applyTemplate

This function processes template file with parameters file and then runs "oc apply" for the processing results.
Also it has a workaround for the bug that if the DeploymentConfig already exists, it will break the Image Trigger.

Sample usage:

To run against the local:
```
stage {
    steps{
        applyTemplate(projectname: "example", templateFile: "hello-world-template.yaml", parameterFile: "hello-world-params.txt")
    }
}
```
Run against remote cluster:
```
stage {
    steps{
        applyTemplate(projectname: "example", templateFile: "hello-world-template.yaml", parameterFile: "hello-world-params.txt", clusterUrl: "https://master.example.com", clusterToken: "KUBERNETES TOKEN")
    }
}
```

Include this library by adding this before "pipeline" in your Jenkins:
```
library identifier: "pipeline-library@master", retriever: modernSCM(
  [$class: "GitSCMSource",
   remote: "https://github.com/redhat-cop/pipeline-library.git"])
```