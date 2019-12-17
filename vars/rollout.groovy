#!/usr/bin/env groovy

class RolloutInput implements Serializable {
    //Required
    String deploymentConfigName = ""
    String resourceKindAndName = ""

    //Optional
    boolean latest = true

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
    if (input.deploymentConfigName?.trim()?.length() > 0) {
        echo "deploymentConfig is deprecated. Please use 'resourceKindAndName'"

        input.resourceKindAndName = input.deploymentConfigName
    }

    assert input.resourceKindAndName?.trim() : "Param resourceKindAndName should be defined."

    openshift.loglevel(input.loglevel)

    openshift.withCluster(input.clusterAPI, input.clusterToken) {
        openshift.withProject(input.projectName) {
            echo "Attemping to rollout latest '${input.resourceKindAndName}' in ${openshift.project()}"

            def resource = openshift.selector(input.resourceKindAndName)
            def rolloutManager = resource.rollout()

            if (input.latest) {
                rolloutManager.latest()
            }

            echo "Waiting for rollout of '${input.resourceKindAndName}' in ${openshift.project()} to complete..."

            try {
                rolloutManager.status("--watch=true")
            } catch (ex) {
                //Something went wrong, so lets print out some helpful information
                rolloutManager.history()
                resource.describe()

                throw ex
            }
        }
    }
}