#!/usr/bin/env groovy

class ClusterInput implements Serializable {
    //Required
    String targetApp

    //Optional - Platform
    String clusterUrl = ""
    String clusterAPI = ""
    String clusterToken = ""
    String projectName = ""
    Integer loglevel = 0
}

// verify deployment
def call(Map input) {
    call(new ClusterInput(input))
}

def call(ClusterInput input) {
    assert input.targetApp?.trim(): "Param targetApp should be defined."

    openshift.loglevel(input.loglevel)

    if (input.clusterUrl?.trim().length() > 0) {
        error "clusterUrl is deprecated and will be removed in the next release. Please use 'clusterAPI'"
    }

    rollout([
        clusterAPI         : input.clusterAPI,
        clusterToken       : input.clusterToken,
        projectName        : input.projectName,
        resourceKindAndName: "dc/${input.targetApp}",
        latest             : false
    ])
}
