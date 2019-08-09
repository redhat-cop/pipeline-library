# Verify Deployment

Verifies OpenShift deployment by checking each deployed pods status.

Sample usage:

```
stage {
    steps{
        verifyDeployment(targetApp: "hello-world", projectName: "example-project")
    }
}
```

Example with all parameters set:
```
stage {
    steps{
        verifyDeployment(targetApp: "hello-world", 
                 projectName: "example-project", 
                 clusterUrl: "https://master.example.com",
                 clusterToken: "<Openshift token>")
    }
}
```

Include this library by adding this before "pipeline" in your Jenkins:
```
library identifier: "pipeline-library@master", retriever: modernSCM(
  [$class: "GitSCMSource",
   remote: "https://github.com/redhat-cop/pipeline-library.git"])
```