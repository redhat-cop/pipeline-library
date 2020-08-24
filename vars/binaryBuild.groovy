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
            def build = buildConfig.startBuild("${input.buildFromFlag}=${input.buildFromPath}", '--wait')
            if (build.names().size() > 1) {
                def buildConfigObject = buildConfig.object()
                def buildVersion = buildConfigObject.status?.lastVersion
                def latestBuild = openshift.selector("build", "${input.buildConfigName}-${buildVersion}")
                latestBuild.logs('-f')
            } else {
                build.logs('-f')
            }
        }
    }
}
