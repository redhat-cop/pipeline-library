#!/usr/bin/env groovy

class ClusterCredentialsInput implements Serializable {
    //Required
    String secretName  = ""

    //Optional
    boolean insecure = false

    //Optional - Platform
    String clusterAPI = ""
    String clusterToken = ""
    String projectName = ""
    Integer loglevel = 0
}

def call(Map input) {
    call(new ClusterCredentialsInput(input))
}

def call(ClusterCredentialsInput input) {
    assert input.secretName?.trim()  : "Param secretName should be defined."

    openshift.loglevel(input.loglevel)

    def encodedApi
    def encodedToken

    openshift.withCluster(input.clusterAPI, input.clusterToken) {
        openshift.withProject(input.projectName) {
            echo "Attemping to retrieve ClusterCredentials 'secret/${input.secretName}' in ${openshift.project()}"

            def secret = openshift.selector("secret/${input.secretName}")
            def secretObject = secret.object()
            def secretData = secretObject.data

            encodedApi = secretData.api
            encodedToken = secretData.token
        }
    }

    def api = sh(script:"set +x; echo ${encodedApi} | base64 --decode", returnStdout: true)
    def token = sh(script:"set +x; echo ${encodedToken} | base64 --decode", returnStdout: true)

    //NOTE: the regex here makes it so that the jenkins-client-plugin wont verify the CA
    api = input.insecure ? api.replaceAll(/https?/, 'insecure') : api

    return [api, token]
}
