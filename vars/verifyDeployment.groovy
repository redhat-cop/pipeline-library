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
        error "clusterUrl is deprecated and will be removed in the next release. Please use 'clusterAPI'"
    }

    openshift.withCluster(input.clusterAPI, input.clusterToken) {
        openshift.withProject(input.projectName) {
            echo "Attempting to verify 'deploymentconfig/${input.targetApp}' in ${openshift.project()}"

            def deploymentConfig = openshift.selector("dc", input.targetApp)
            def dcObj = deploymentConfig.object()
            def podSelector = openshift.selector("pod", [deployment: "${input.targetApp}-${dcObj.status.latestVersion}"])
            podSelector.untilEach { pod ->
                echo "Checking pod '${pod.object().metadata.name}' attached to 'deploymentconfig/${input.targetApp}' in ${openshift.project()} is ready"

                pod.object().status.containerStatuses.every {
                    if(it.state.waiting != null) {
                        if(it.state.waiting.reason == "CrashLoopBackOff") {
                            echo "Container failing to start. Logs:"
                            pod.logs()
                            deploymentConfig.rollout().cancel()
                            error "CrashLoopBackOff"
                        }
                        else if(it.state.waiting.reason == "CreateContainerConfigError") {
                            def message = it.state.waiting.message
                            echo "Container cannot be created: ${message}"
                            deploymentConfig.rollout().cancel()
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
