#!/usr/bin/env groovy

class CopyImageInput implements Serializable {
    //Required
    String sourceImageName
    String sourceImageTag = "latest"
    String destinationImageName
    String destinationImageTag
    String destinationImagePath
    String targetRegistryCredentials = "other-cluster-credentials"

    //Optional
    String sourceImagePath = ""

    //Optional - Platform
    String clusterUrl = ""
    String clusterAPI = ""
    String clusterToken = ""

    CopyImageInput init() {
        if(!destinationImageName?.trim()) destinationImageName = sourceImageName
        if(!destinationImageTag?.trim()) destinationImageTag = sourceImageTag
        if(!destinationImagePath?.trim()) destinationImagePath = sourceImagePath
        return this
    }
}

def call(Map input) {
    call(new CopyImageInput(input).init())
}

def call(CopyImageInput input) {
    assert input.targetRegistryCredentials?.trim()  : "Param targetRegistryCredentials should be defined."
    assert input.sourceImageName?.trim()  : "Param sourceImageName should be defined."
    assert input.sourceImageTag?.trim()  : "Param sourceImageTag should be defined."
    assert input.destinationImagePath?.trim()  : "Param destinationImagePath should be defined."
    assert input.destinationImageName?.trim()  : "Param destinationImageName should be defined."
    assert input.destinationImageTag?.trim()  : "Param destinationImageTag should be defined."

    if (input.clusterUrl?.trim().length() > 0) {
        echo "WARNING: clusterUrl is deprecated. Please use 'clusterAPI'"

        input.clusterAPI = input.clusterUrl
    }

    openshift.withCluster(input.clusterUrl, input.clusterToken) {
        def localToken = readFile("/var/run/secrets/kubernetes.io/serviceaccount/token").trim()

        def secretData = openshift.selector("secret/${input.targetRegistryCredentials}").object().data
        def registry = sh(script:"set +x; echo ${secretData.registry} | base64 --decode", returnStdout: true)
        def token = sh(script:"set +x; echo ${secretData.token} | base64 --decode", returnStdout: true)
        def username = sh(script:"set +x; echo ${secretData.username} | base64 --decode", returnStdout: true)

        openshift.withProject(input.sourceImagePath) {
            def localRegistry = openshift.selector( "is", "${input.sourceImageName}").object().status.dockerImageRepository
            def from = "docker://${localRegistry}:${input.sourceImageTag}"
            def to = "docker://${registry}/${input.destinationImagePath}/${input.destinationImageName}:${input.destinationImageTag}"

            echo "Now Promoting ${from} -> ${to}"
            sh """
                set +x
                skopeo copy --remove-signatures \
                --src-creds openshift:${localToken} --src-cert-dir=/run/secrets/kubernetes.io/serviceaccount/ \
                --dest-creds ${username}:${token}  --dest-tls-verify=false ${from} ${to}
            """
        }
    }
}
