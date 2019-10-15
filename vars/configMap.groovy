#!/usr/bin/env groovy

class ConfigMapInput implements Serializable {
    //Required
    String configMapName  = ""

    //Optional - Platform
    String clusterAPI = ""
    String clusterToken = ""
    String projectName = ""
    Integer loglevel = 0
}

def call(Map input) {
    call(new ConfigMapInput(input))
}

def call(ConfigMapInput input) {
    assert input.configMapName?.trim()  : "Param configMapName should be defined."

    openshift.loglevel(input.loglevel)

    def configMapData

    openshift.withCluster(input.clusterAPI, input.clusterToken) {
        openshift.withProject(input.projectName) {
            echo "Attemping to retrieve 'configmap/${input.configMapName}' in ${openshift.project()}"

            def configMap = openshift.selector("configmap/${input.configMapName}")
            def configMapObject = configMap.object()
            configMapData = configMapObject.data
        }
    }
     
    return configMapData
}
