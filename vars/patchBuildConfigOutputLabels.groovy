#!/usr/bin/env groovy

// When using the non-declarative pipeline, git env variables need to be set through scm checkout
class PatchBuildConfigOutputLabelsInput implements Serializable{
    String domainPrefix = "com.redhat"
    String bcName
}

def call(Map input) {
    call(new PatchBuildConfigOutputLabelsInput(input))
} 

def call(PatchBuildConfigOutputLabelsInput input) {
    assert input.bcName?.trim() : "Param bcName (build config name) should be defined"

    def patch = [
            spec: [
                output : [
                    imageLabels : [ 
                        [ name: "${input.domainPrefix}.jenkins.build.url", value: "${env.BUILD_URL}" ],
                        [ name: "${input.domainPrefix}.jenkins.build.tag", value: "${env.BUILD_NUMBER}"],
                        [ name: "${input.domainPrefix}.git.branch", value: "${env.GIT_BRANCH}"],
                        [ name: "${input.domainPrefix}.git.url", value: "${env.GIT_URL}"],
                        [ name: "${input.domainPrefix}.git.commit", value: "${env.GIT_COMMIT}"]
                    ]
                ]
            ] 
        ]

    def patchJson = groovy.json.JsonOutput.toJson(patch)

    sh "oc patch bc ${input.bcName} -p '${patchJson}'"
}