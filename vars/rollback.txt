# Rollback deployment

Provide the deployment config to rollback to the latest or a specific revision.

Sample usage:

Run against local cluster and rollback to latest successful deployment:
```
stage {
        steps {
            rollback(deploymentConfig: "dc/spring-boot-rest-example")
        }
    }
```

Run against local cluster and rollback to specific revision:
```
stage {
        steps {
            rollback(deploymentConfig: "dc/spring-boot-rest-example", rollbackVersion: "10")
        }
    }
```

Run against remote cluster and all parameters set:
```
stage {
        steps {
            rollback(deploymentConfig: "dc/spring-boot-rest-example", rollbackVersion: "10", clusterUrl: "https://master.example.com", clusterToken: "KUBERNETES TOKEN")
        }
    }
```

Include this library by adding this before "pipeline" in your Jenkins:
```
library identifier: "pipeline-library@master", retriever: modernSCM(
  [$class: "GitSCMSource",
   remote: "https://github.com/redhat-cop/pipeline-library.git"])
```
