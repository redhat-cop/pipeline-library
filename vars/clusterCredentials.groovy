#!/usr/bin/env groovy

class ClusterCredentialsInput implements Serializable {
    //Required
    String secretName  = ""

    //Optional - Platform
    String clusterAPI = ""
    String clusterToken = ""
    String projectName = ""
}

def call(Map input) {
    call(new ClusterCredentialsInput(input))
}

def call(ClusterCredentialsInput input) {
    assert input.secretName?.trim()  : "Param secretName should be defined."

    echo "Get Cluster Credentials: ${input.projectName}/${input.secretName}"

    def encodedApi
    def encodedToken

    openshift.withCluster(input.clusterAPI, input.clusterToken) {
        openshift.withProject(input.projectName) {
            def secret = openshift.selector("secret/${input.secretName}")
            def secretObject = secret.object()
            def secretData = secretObject.data

            encodedApi = secretData.api
            encodedToken = secretData.token
        }
    }
     
    //NOTE: the regex here makes it so that the jenkins-client-plugin wont verify the CA
    def api      = sh(script:"set +x; echo ${encodedApi}      | base64 --decode", returnStdout: true).replaceAll(/https?/, 'insecure')
    def token    = sh(script:"set +x; echo ${encodedToken}    | base64 --decode", returnStdout: true)

    return [api, token]
}
