#!/usr/bin/env groovy

class BuildAndTagInput implements Serializable {
    String imageName               = ''
    String imageNamespace          = ''
    String imageVersion            = ''
    String registryFQDN            = ''
    String fromFilePath            = ''
    String tagDestinationTLSVerify = 'false'
    String tagSourceTLSVerify      = 'false'

    //Optional - Platform
    String clusterAPI              = ""
    String clusterToken            = ""
    String buildProjectName        = ""
}

def call(Map input) {
    call(new BuildAndTagInput(input))
}

def call(BuildAndTagInput input) {
    assert input.imageName?.trim()        : "Param imageName should be defined."
    assert input.imageNamespace?.trim()   : "Param imageNamespace should be defined."
    assert input.imageVersion?.trim()     : "Param imageVersion should be defined."
    assert input.registryFQDN?.trim()     : "Param registryFQDNshould be defined."
    assert input.fromFilePath?.trim()     : "Param fromFilePath should be defined."

    binaryBuild([
        clusterAPI     : input.clusterAPI,
        clusterToken   : input.clusterToken,
        projectName    : input.buildProjectName,
        buildConfigName: input.imageName,
        buildFromPath   : input.fromFilePath
    ])

    echo "Tag for Build"
    sh """
        skopeo copy  \
            --authfile /var/run/secrets/kubernetes.io/dockerconfigjson/.dockerconfigjson \
            --src-tls-verify=${input.tagSourceTLSVerify} \
            --dest-tls-verify=${input.tagDestinationTLSVerify} \
            docker://${input.registryFQDN}/${input.imageNamespace}/${input.imageName}:latest docker://${input.registryFQDN}/${input.imageNamespace}/${input.imageName}:${input.imageVersion}
    """
}
