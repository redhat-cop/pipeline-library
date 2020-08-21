#!/usr/bin/env groovy

class BinaryBuildInput implements Serializable {
    //Required
    String buildConfigName = ""
    String buildFromFlag   = "--from-dir"
    String buildFromPath   = ""

    //Optional - Platform
    String clusterAPI      = ""
    String clusterToken    = ""
    String projectName     = ""
    Integer loglevel = 0
}

def call(Map input) {
    call(new BinaryBuildInput(input))
}

def call(BinaryBuildInput input) {
    assert input.buildConfigName?.trim() : "Param buildConfigName should be defined."
    assert input.buildFromFlag?.trim()   : "Param buildFromFlag should be defined."
    assert input.buildFromPath?.trim()   : "Param buildFromPath should be defined."

    openshift.loglevel(input.loglevel)

    openshift.withCluster(input.clusterAPI, input.clusterToken) {
        openshift.withProject(input.projectName) {
            echo "Attemping to start and follow 'buildconfig/${input.buildConfigName}' in ${openshift.project()}"

            def buildConfig = openshift.selector('bc', input.buildConfigName)
            buildConfig.startBuild("${input.buildFromFlag}=${input.buildFromPath}", '--wait')
            def build = buildConfig.related('builds')
            build.logs('-f')
        }
    }
}
