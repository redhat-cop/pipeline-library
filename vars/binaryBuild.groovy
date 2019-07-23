#!/usr/bin/env groovy

class BinaryBuildInput implements Serializable {
    String clusterAPI      = ""
    String clusterToken    = ""
    String projectName     = ""
    String buildConfigName = ""
    String buildFromFlag   = "--from-dir"
    String buildFromPath   = ""
}

def call(Map input) {
    call(new BinaryBuildInput(input))
}

def call(BinaryBuildInput input) {
    assert input.projectName?.trim()     : "Param projectName should be defined."
    assert input.buildConfigName?.trim() : "Param buildConfigName should be defined."
    assert input.buildFromFlag?.trim()   : "Param buildFromFlag should be defined."
    assert input.buildFromPath?.trim()   : "Param buildFromPath should be defined."

    openshift.withCluster(input.clusterAPI, input.clusterToken) {
        openshift.withProject(input.projectName) {
            echo "Start & Follow Build"
            def buildConfig = openshift.selector('bc', input.buildConfigName)
            def build       = buildConfig.startBuild("${input.buildFromFlag}=${input.buildFromPath}", '--wait')
            build.logs('-f')
        }
    }
}
