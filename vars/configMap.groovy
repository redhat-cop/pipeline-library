#!/usr/bin/env groovy

class ConfigMapInput implements Serializable {
    String configMapName  = ""

    //Optional - Platform
    String clusterAPI = ""
    String clusterToken = ""
    String projectName = ""
}

def call(Map input) {
    call(new ConfigMapInput(input))
}

def call(ConfigMapInput input) {
    assert input.projectName?.trim() : "Param projectName should be defined."
    assert input.configMapName?.trim()  : "Param configMapName should be defined."

    echo "Read ConfigMap: ${input.projectName}/${input.configMapName}"

    def configMapData

    openshift.withCluster(input.clusterAPI, input.clusterToken) {
        openshift.withProject(input.projectName) {
            def configMap = openshift.selector("configmap/${input.configMapName}")
            def configMapObject = configMap.object()
            configMapData = configMapObject.data
        }
    }
     
    return configMapData
}
