# configMap

Read an OpenShift ConfigMap into a groovy map. The ConfigMap must be in the same OpenShift cluster that the Jenkins agent is running in.

# Sample Usage

```
def configMapData = configMap(
    projectName     : 'my-project',
    configMapName   : 'my-build-config'
)
echo "configMapData: '${configMapData}"
echo "configMapData.my-entry: '${configMapData.my-entry}"
```
