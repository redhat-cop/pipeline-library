#!/usr/bin/env groovy

import groovy.json.JsonOutput

// When using the non-declarative pipeline, git env variables need to be set through scm checkout
class PatchBuildConfigOutputLabelsInput implements Serializable {
    //Required
    String domainPrefix = "com.redhat"
    String bcName = ""

    //Optional
    String clusterAPI = ""
    String clusterToken = ""
    String projectName = ""
}

def call(Map input) {
    call(new PatchBuildConfigOutputLabelsInput(input))
}

def call(PatchBuildConfigOutputLabelsInput input) {
    assert input.domainPrefix?.trim(): "Param domainPrefix should be defined"
    assert input.bcName?.trim(): "Param bcName (build config name) should be defined"

    def patch = [
            spec: [
                output: [
                    imageLabels: [
                        [name: "${input.domainPrefix}.jenkins.build.url", value: "${env.BUILD_URL}"],
                        [name: "${input.domainPrefix}.jenkins.build.tag", value: "${env.BUILD_NUMBER}"],
                        [name: "${input.domainPrefix}.git.branch", value: "${env.GIT_BRANCH}"],
                        [name: "${input.domainPrefix}.git.url", value: "${env.GIT_URL}"],
                        [name: "${input.domainPrefix}.git.commit", value: "${env.GIT_COMMIT}"]
                    ]
                ]
            ]
        ]

    openshift.withCluster(input.clusterAPI, input.clusterToken) {
        openshift.withProject(input.projectName) {
            def buildConfig = openshift.selector("bc", input.bcName)
            if (buildConfig.exists()) {
                openshift.patch(buildConfig.object(), "'" + JsonOutput.toJson(patch) + "'")
            } else {
                error "Failed to find 'bc/${input.bcName}' in ${openshift.project()}"
            }
        }
    }
}