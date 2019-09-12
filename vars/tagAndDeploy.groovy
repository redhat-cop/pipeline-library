#!/usr/bin/env groovy

class TagAndDeployInput implements Serializable {
    String imageName                    = ''
    String imageNamespace               = ''
    String imageVersion                 = ''
    String registryFQDN                 = ''
    String deployDestinationProjectName = ''
    String deployDestinationVersionTag  = ''  
    String tagDestinationTLSVerify      = 'true'
    String tagSourceTLSVerify           = 'true'
    String tagAuthFile                  = "/var/run/secrets/kubernetes.io/dockerconfigjson/.dockerconfigjson"
    String tagDestinationCertDir        = "/run/secrets/kubernetes.io/serviceaccount/"
    String tagSourceCertDir             = "/run/secrets/kubernetes.io/serviceaccount/"

    //Optional - Platform
    String clusterAPI                   = ''
    String clusterToken                 = ''
}

def call(Map input) {
    call(new TagAndDeployInput(input))
}

def call(TagAndDeployInput input) {
    assert input.imageName?.trim()                : "Param imageName should be defined."
    assert input.imageNamespace?.trim()           : "Param imageNamespace should be defined."
    assert input.imageVersion?.trim()             : "Param imageVersion should be defined."
    assert input.registryFQDN?.trim()             : "Param registryFQDN should be defined."
    assert input.deployDestinationProjectName?.trim()  : "Param deployDestinationProjectName should be defined."
    assert input.deployDestinationVersionTag?.trim()      : "Param deployDestinationVersionTag should be defined."

    echo "Tag ${input.imageNamespace}/${input.imageName}:${input.imageVersion} as ${input.imageNamespace}/${input.imageName}:${input.deployDestinationVersionTag}"

    def authFileArg = input.tagAuthFile?.trim()?.length() <= 0 ? "" : "--authfile=${input.tagAuthFile}"
    def srcTlsVerifyArg = input.tagSourceTLSVerify?.trim()?.length() <= 0 ? "" : "--src-tls-verify=${input.tagSourceTLSVerify}"
    def destTlsVerifyArg = input.tagDestinationTLSVerify?.trim()?.length() <= 0 ? "" : "--dest-tls-verify=${input.tagDestinationTLSVerify}"
    def destCertDirArg = input.tagDestinationCertDir?.trim()?.length() <= 0 ? "" : "--dest-cert-dir=${input.tagDestinationCertDir}"
    def srcCertDirArg = input.tagSourceCertDir?.trim()?.length() <= 0 ? "" : "--src-cert-dir=${input.tagSourceCertDir}"

    sh """
        skopeo copy $authFileArg $srcTlsVerifyArg $destTlsVerifyArg $destCertDirArg $srcCertDirArg \
            docker://${input.registryFQDN}/${input.imageNamespace}/${input.imageName}:${input.imageVersion} docker://${input.registryFQDN}/${input.imageNamespace}/${input.imageName}:${input.deployDestinationVersionTag}
    """

    echo "Deploy to ${input.deployDestinationProjectName}"
    rollout(
        clusterAPI     : input.clusterAPI,
        clusterToken   : input.clusterToken,
        projectName    : input.deployDestinationProjectName,
        deploymentConfigName: input.imageName
    )
}
