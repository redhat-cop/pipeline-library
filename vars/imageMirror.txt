# Image Mirror

Image mirror allows you to move images from one image registry to another.

Sample Usage:

```
stage ('Image Mirror') {
  steps {
    imageMirror(sourceSecret: "namespace-sourcesecret",
                sourceRegistry: "https://source-registry.com",
                destinationSecret: "namespace-destinationsecret",
                destinationRegistry: "https://destination-registry.com",
                insecure: "true",
                sourceNamespace: "source-namespace",
                destinationNamespace: "destination-namespace",
                sourceImage: "source-image",
                destinationImage: "destination-image",
                sourceImageVersion: "latest",
                destinationImageVersion: "latest"
                )
  }
}
```

Example Secret Template:

```
apiVersion: v1
kind: Template
labels:
  template: image-mirror-secret
metadata:
  annotations:
    description: Cluster Credential Secret
    tags: secret
    version: 1.0.0
  name: image-mirror-secret
objects:
- apiVersion: v1
  stringData:
    token: "${TOKEN}"
    username: generic
    password: "${TOKEN}"
  data:
  kind: Secret
  metadata:
    name: ${SECRET_NAME}
    labels:
      credential.sync.jenkins.openshift.io: 'true'
  type: kubernetes.io/basic-auth
parameters:
- description: The name for the application.
  name: SECRET_NAME
  required: true
- description: Service Account Token
  name: TOKEN
  required: true
```


Include this library by adding this before "pipeline" in your Jenkins:
```
library identifier: "pipeline-library@master", retriever: modernSCM(
  [$class: "GitSCMSource",
   remote: "https://github.com/redhat-cop/pipeline-library.git"])
```