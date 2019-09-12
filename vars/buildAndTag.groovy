#!/usr/bin/env groovy

class BuildAndTagInput implements Serializable {
    //Required
    String imageName               = ''
    String imageNamespace          = ''
    String imageVersion            = ''
    String registryFQDN            = ''

    //Optional
    String fromFilePath            = ""
    String tagDestinationTLSVerify = "true"
    String tagSourceTLSVerify      = "true"
    String tagAuthFile             = "/var/run/secrets/kubernetes.io/dockerconfigjson/.dockerconfigjson"
    String tagDestinationCertDir   = "/run/secrets/kubernetes.io/serviceaccount/"
    String tagSourceCertDir        = "/run/secrets/kubernetes.io/serviceaccount/"

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
    assert input.registryFQDN?.trim()     : "Param registryFQDN should be defined."

    binaryBuild([
        clusterAPI     : input.clusterAPI,
        clusterToken   : input.clusterToken,
        projectName    : input.buildProjectName,
        buildConfigName: input.imageName,
        buildFromPath   : input.fromFilePath
    ])

    def authFileArg = input.tagAuthFile?.trim()?.length() <= 0 ? "" : "--authfile=${input.tagAuthFile}"
    def srcTlsVerifyArg = input.tagSourceTLSVerify?.trim()?.length() <= 0 ? "" : "--src-tls-verify=${input.tagSourceTLSVerify}"
    def destTlsVerifyArg = input.tagDestinationTLSVerify?.trim()?.length() <= 0 ? "" : "--dest-tls-verify=${input.tagDestinationTLSVerify}"
    def destCertDirArg = input.tagDestinationCertDir?.trim()?.length() <= 0 ? "" : "--dest-cert-dir=${input.tagDestinationCertDir}"
    def srcCertDirArg = input.tagSourceCertDir?.trim()?.length() <= 0 ? "" : "--src-cert-dir=${input.tagSourceCertDir}"

    def source = "docker://${input.registryFQDN}/${input.imageNamespace}/${input.imageName}:latest"
    def destination = "docker://${input.registryFQDN}/${input.imageNamespace}/${input.imageName}:${input.imageVersion}"

    echo "Attempting to tag; ${source} -> ${destination}"

    sh "skopeo copy $authFileArg $srcTlsVerifyArg $destTlsVerifyArg $destCertDirArg $srcCertDirArg $source $destination"
}
