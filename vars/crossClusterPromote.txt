# Cross Cluster Promotion

Promotes container image cross OpenShift clusters using Skopeo.
Requires a Jenkins Slave container image that include Skopeo tool.

To be able to use this, you will need to have a secret with following keys:
* registry - URL for the container registry where we are pushing image to
* username - Username for pushing the image, must be set as Skopeo require it even if target registry doesn't use it.
* token - Authentication token for the target registry

Sample usage:

Simples example that assumes that above secret is called "other-cluster-credentials":
```
stage {
    steps{
        crossClusterPromote(sourceImageName: "hello-world", sourceImagePath: "example-project")
    }
}
```

With all parameters
```
stage {
    steps{
        crossClusterPromote(sourceImageName: "hello-world", 
							sourceImagePath: "example-project", 
							sourceImageTag: "release-ready", 
							destinationImageName: "hello-world2", 
							destinationImageTag: "released",
							destinationImagePath: "target-project",
							targetRegistryCredentials: "<Openshift secret name>"
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