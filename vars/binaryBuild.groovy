#!/usr/bin/env groovy

class BinaryBuildInput implements Serializable {
    String projectName
    String buildConfigName
    String artifactsDirectoryName
    String buildFrom = "--from-dir"

    BinaryBuildInput init() {
        if(!artifactsDirectoryName?.trim()) artifactsDirectoryName = "deploy"
        return this
    }
}

def call (Map input) {
    call(new BinaryBuildInput(input).init())
}

def call(BinaryBuildInput input) {
    openshift.withCluster() {
        openshift.withProject("${input.projectName}") {
            openshift.selector("bc", "${input.buildConfigName}").startBuild("${input.buildFrom}=${input.artifactsDirectoryName}", "--wait").logs('-f')
        }        
    }
}