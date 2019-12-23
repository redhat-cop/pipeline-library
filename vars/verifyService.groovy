#!/usr/bin/env groovy

class VerifyServiceInput implements Serializable {
    //Required
    String serviceName = ""

    //Optional - Platform
    String clusterAPI = ""
    String clusterToken = ""
    String projectName = ""
    Integer loglevel = 0
}

def call(Map input) {
    call(new VerifyServiceInput(input))
}

def call(VerifyServiceInput input) {
    assert input.serviceName?.trim(): "Param serviceName should be defined."

    openshift.loglevel(input.loglevel)

    openshift.withCluster(input.clusterAPI, input.clusterToken) {
        openshift.withProject(input.projectName) {
            def connected = openshift.verifyService(input.serviceName)
            if (!connected) {
                error "Failed to connect to service: '${input.serviceName}' in ${openshift.project()}"
            }
        }
    }
}
