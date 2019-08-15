# clusterCredentials

Gets the credentials for a cluster from a given secret in the given namespace.

## Template for Creating Expected Secret

```
---
kind: Template
apiVersion: v1
metadata:
  name: cluster-credential-secret
  annotations:
    openshift.io/display-name: Cluster Credential Secret
objects:
- kind: Secret
  apiVersion: v1
  metadata:
    name: "${NAME}"
  type: Opaque
  stringData:
    api: "${API}"
    token: "${TOKEN}"
parameters:
- name: NAME
  displayName: Name
  description: The name of secret.
  required: true
- name: API
  displayName: API
  description: API url of the cluster the credential is for.
  required: true
- name: TOKEN
  displayName: Token
  description: Authentication token for the cluster.
  required: true
```

## Sample Usage

```
def (api, token) = clusterCredentials(
    projectName: env.CICD_NAMESPACE,
    secretName:  env.DEV_CLUSTER_CREDENTIAL_SECRET_NAME
)
```
