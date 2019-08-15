# tagAndDeploy
First this function tags a given image in a given remote image repository with a given tag and then it invokes a rollout of a given deployment configuration.

## Sample Usage

```
tagAndDeploy(
    imageName                    : 'my-service',
    imageNamespace               : 'my-app',
    imageVersion                 : '1.0',
    registryName                 : 'my-registry.example.com',
    clusterAPI                   : 'ocp.example.com'
    clusterToken                 : 'ocp-user-token'
    deployDestinationProjectName : 'dev'
    deployDestinationVersionTag  : 'dev'
    tagDestinationTLSVerify      : 'false'
    tagSourceTLSVerify           : 'false'
)
```
