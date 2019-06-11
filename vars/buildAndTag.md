# buildAndTag
Invokes an image build and tag

## Sample Usage

```
buildAndTag(
    imageName        : 'my-service',
    imageNamespace   : 'my-app',
    imageVersion     : '1.0',
    registryName     : 'my-registry.example.com',
    buildProjectName : 'my-cicd-namespace',
    fromFilePath     : 'target/my-artifact'
)
```
