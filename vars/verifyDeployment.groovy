#!/usr/bin/env groovy

class ClusterInput implements Serializable{
    String projectName
    String targetApp
    String clusterUrl = ""
    String clusterToken = ""
}

// verify deployment
def call(Map input) {
    call(new ClusterInput(input))
}

def call(ClusterInput input) {
    openshift.withCluster(input.clusterUrl, input.clusterToken) {
        openshift.withProject("${input.projectName}") {
            def dcObj = openshift.selector("dc", input.targetApp).object()
            def podSelector = openshift.selector("pod", [deployment: "${input.targetApp}-${dcObj.status.latestVersion}"])
            podSelector.untilEach {
            echo "pod: ${it.name()}"
            return it.object().status.containerStatuses[0].ready
            }
        }
    }
}
