#!/usr/bin/env groovy

class RestartPodsInput implements Serializable {
    String projectName = ""
    String targetApp = ""
    String clusterUrl = ""
    String clusterToken = ""
}

def call (Map input) {
    call(new RestartPodsInput(input))
}

def call(RestartPodsInput input) {

    assert input.targetApp?.trim() : "Param targetApp should be defined."
    assert input.projectName?.trim() : "Param projectName should be defined."

    openshift.withCluster(input.clusterUrl, input.clusterToken) {
        openshift.withProject(input.projectName) {
            openshift.selector("rc", input.targetApp).restart()
        }
    }
}