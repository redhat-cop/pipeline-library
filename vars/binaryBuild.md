# binaryBuild

This does an OpenShift binary build from a given path.

## Sample Usage

### Build From Directory

```
binaryBuildFromFile(
    projectName:     "hello-world-cicd",
    buildConfigName: "service1",
    buildFromFlag:   "--from-dir",
    buildFromPath:   "target/"
)
```

### Build From File

```
binaryBuildFromFile(
    projectName:     "hello-world-cicd",
    buildConfigName: "service1",
    buildFromFlag:   "--from-file",
    buildFromPath:   "target/service1.war"
)
```

### Build From Directory on Remote Cluster

```
binaryBuildFromFile(
    clusterAPI:      "my-remote-ocp-cluster.example.com",
    clusterToken:    "my-remote-cop-cluster-auth-token",
    projectName:     "hello-world-cicd",
    buildConfigName: "service1",
    buildFromFlag:   "--from-file",
    buildFromPath:   "target/service1.war"
)
```
