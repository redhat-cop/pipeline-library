#!/usr/bin/env groovy

class ClusterInput implements Serializable {
    //Required
    String targetApp

    //Optional - Platform
    String clusterUrl = ""
    String clusterAPI = ""
    String clusterToken = ""
    String projectName = ""
}

// verify deployment
def call(Map input) {
    call(new ClusterInput(input))
}

def call(ClusterInput input) {
    assert input.targetApp?.trim() : "Param targetApp should be defined."

    if (input.clusterUrl?.trim().length() > 0) {
        echo "WARNING: clusterUrl is deprecated. Please use 'clusterAPI'"

        input.clusterAPI = input.clusterUrl
    }

    openshift.withCluster(input.clusterUrl, input.clusterToken) {
        openshift.withProject(input.projectName) {
            def dcObj = openshift.selector("dc", input.targetApp).object()
            def podSelector = openshift.selector("pod", [deployment: "${input.targetApp}-${dcObj.status.latestVersion}"])
            podSelector.untilEach { pod ->
                pod.object().status.containerStatuses.every {
                    if(it.state.waiting != null) {
                        if(it.state.waiting.reason == "CrashLoopBackOff") {
                            echo "Container failing to start. Logs:"
                            pod.logs()
                            sh "oc deploy ${appName} --cancel"
                            error "CrashLoopBackOff"
                        }
                        else if(it.state.waiting.reason == "CreateContainerConfigError") {
                            def message = it.state.waiting.message
                            echo "Container cannot be created: ${message}"
                            sh "oc deploy ${appName} --cancel"
                            error "CreateContainerConfigError : ${message}"
                        }
                    }
                    return pod.object().status.containerStatuses.every {
                        it.ready
                    }
                }                
            }
        }
    }
}
