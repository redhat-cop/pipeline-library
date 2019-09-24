#!/usr/bin/env groovy

class ConfigMapInput implements Serializable {
    //Required
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
    assert input.configMapName?.trim()  : "Param configMapName should be defined."

    def configMapData

    openshift.withCluster(input.clusterAPI, input.clusterToken) {
        openshift.withProject(input.projectName) {
            echo "Read ConfigMap: ${openshift.project()}/${input.configMapName}"

            def configMap = openshift.selector("configmap/${input.configMapName}")
            def configMapObject = configMap.object()
            configMapData = configMapObject.data
        }
    }
     
    return configMapData
}
