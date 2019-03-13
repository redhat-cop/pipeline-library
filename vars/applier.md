# applier

Applier executes templates on a specified cluster.

Parameters:

applier(secretName: String (required),
        registryUrl: String (required),
        inventoryPath: String (required),
        requirementsPath: String (required),
        skipTlsVerify: boolean (optional))

Sample usage:

To run against the local:
```
podTemplate(label: 'applier', cloud: 'openshift', containers: [
    containerTemplate(name: 'applier', image: 'quay.io/redhat-cop/openshift-applier:latest', ttyEnabled: true, command: 'cat')
]) {

  node('applier') {

    stage('SCM Checkout') {
        checkout scm
    }

    stage ("Run Applier") {
        applier(secretName: "mysecret", 
                registryUrl: "https://myregistry.com",
                inventoryPath: ".applier/inventory",
                requirementsPath: ".applier/requirements.yml",
                skipTlsVerify: true )
      }
  }
}
```

Include this library by adding this before "pipeline" in your Jenkins:
```
library identifier: "pipeline-library@master", retriever: modernSCM(
  [$class: "GitSCMSource",
   remote: "https://github.com/redhat-cop/pipeline-library.git"])
```

Example Service Account Template:

```
apiVersion: v1
kind: Template
metadata:
  annotations:
    description: template for image mirror service account
objects:
- apiVersion: v1
  kind: ServiceAccount
  metadata:
    name: ${SA_NAME}
    namespace: ${NAMESPACE}
- apiVersion: v1
  groupNames: null
  kind: RoleBinding
  metadata:
    creationTimestamp: null
    name: edit
    namespace: ${NAMESPACE}
  roleRef:
    name: edit
  subjects:
  - kind: ServiceAccount
    name: ${SA_NAME}
    namespace: ${NAMESPACE}
  userNames:
  - system:serviceaccount:${NAMESPACE}:${SA_NAME}
parameters:
- description: The namespace to deploy into
  name: NAMESPACE
  required: true
- description: The service account name
  name: SA_NAME
  required: true
```

Example Secret Template:

In order to get the TOKEN, you can do `oc serviceaccounts get-token mysecret`. The service account is created from the template above.

```
apiVersion: v1
kind: Template
labels:
  template: cluster-credentials-secret
metadata:
  annotations:
    description: Cluster Credential Secret
    tags: secret
    version: 1.0.0
  name: cluster-credentials-secret
objects:
- apiVersion: v1
  stringData:
    token: "${TOKEN}"
  data:
  kind: Secret
  metadata:
    name: ${SECRET_NAME}
  type: Opaque
parameters:
- description: The name for the application.
  name: SECRET_NAME
  required: true
- description: Service Account Token
  name: TOKEN
  required: true
```