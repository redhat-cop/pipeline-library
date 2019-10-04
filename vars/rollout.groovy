#!/usr/bin/env groovy

class RolloutInput implements Serializable {
    //Required
    String deploymentConfigName = ""

    //Optional - Platform
    String clusterAPI           = ""
    String clusterToken         = ""
    String projectName          = ""
    Integer loglevel = 0
}

def call(Map input) {
    call(new RolloutInput(input))
}

def call(RolloutInput input) {
    assert input.deploymentConfigName?.trim() : "Param deploymentConfigName should be defined."

    openshift.loglevel(input.loglevel)

    openshift.withCluster(input.clusterAPI, input.clusterToken) {
        openshift.withProject(input.projectName) {
            echo "Attemping to rollout latest 'deploymentconfig/${input.deploymentConfigName}' in ${openshift.project()}"

            def deploymentConfig = openshift.selector('dc', input.deploymentConfigName)
            def rolloutManager   = deploymentConfig.rollout()

            rolloutManager.latest()

            echo "Waiting for rollout of 'deploymentconfig/${input.deploymentConfigName}' in ${openshift.project()} to complete..."

            rolloutManager.status("--wait")
        }
    }
}