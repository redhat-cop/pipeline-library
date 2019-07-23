#!/usr/bin/env groovy

class TagAndDeployInput implements Serializable {
    String imageName                    = ''
    String imageNamespace               = ''
    String imageVersion                 = ''
    String registryFQDN                 = ''
    String clusterAPI                   = ''
    String clusterToken                 = ''
    String deployDestinationProjectName = ''
    String deployDestinationVersionTag  = ''  
    String tagDestinationTLSVerify      = 'false'
    String tagSourceTLSVerify           = 'false'
}

def call(Map input) {
    call(new TagAndDeployInput(input))
}

def call(TagAndDeployInput input) {
    assert input.imageName?.trim()                : "Param imageName should be defined."
    assert input.imageNamespace?.trim()           : "Param imageNamespace should be defined."
    assert input.imageVersion?.trim()             : "Param imageVersion should be defined."
    assert input.registryFQDN?.trim()             : "Param registryFQDN should be defined."
    assert input.clusterAPI?.trim()               : "Param clusterAPI should be defined."
    assert input.clusterToken?.trim()             : "Param clusterToken should be defined."
    assert input.deployDestinationProjectName?.trim()  : "Param deployDestinationProjectName should be defined."
    assert input.deployDestinationVersionTag?.trim()      : "Param deployDestinationVersionTag should be defined."

    container('jenkins-worker-image-mgmt') {
        script {
            echo "Tag ${input.imageNamespace}/${input.imageName}:${input.imageVersion} as ${input.imageNamespace}/${input.imageName}:${input.deployDestinationVersionTag}"
            sh """
                skopeo copy \
                    --authfile /var/run/secrets/kubernetes.io/dockerconfigjson/.dockerconfigjson \
                    --src-tls-verify=${input.tagSourceTLSVerify} \
                    --dest-tls-verify=${input.tagDestinationTLSVerify} \
                    docker://${input.registryFQDN}/${input.imageNamespace}/${input.imageName}:${input.imageVersion} docker://${input.registryFQDN}/${input.imageNamespace}/${input.imageName}:${input.deployDestinationVersionTag}
            """
        }
       
        script {
            echo "Deploy to ${input.deployDestinationProjectName}"
            rollout(
                clusterAPI     : input.clusterAPI,
                clusterToken   : input.clusterToken,
                projectName    : input.deployDestinationProjectName,
                deploymentConfigName: input.imageName
            )
        }
    }
}
