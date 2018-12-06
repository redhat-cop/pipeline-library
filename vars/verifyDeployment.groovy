#!/usr/bin/env groovy

class ClusterInput implements Serializable{
    String projectName
    String targetApp
    String clusterUrl = ""
    String clusterToken = ""
    String secretName = ""
}

// verify deployment
def call(Map input) {
    call(new ClusterInput(input))
}

def call(ClusterInput input) {

    if (input.secretName.length() > 0) {
        script {
            openshift.withCluster() {
                def secretData = openshift.selector("secret/${input.secretName}").object().data
                def encodedToken = secretData.token
                input.clusterToken = sh(script:"set +x; echo ${encodedToken} | base64 --decode", returnStdout: true)
            }
        }
    }

    openshift.withCluster(input.clusterUrl, input.clusterToken) {
        openshift.withProject("${input.projectName}") {
            def dcObj = openshift.selector("dc", input.targetApp).object()
            def podSelector = openshift.selector("pod", [deployment: "${input.targetApp}-${dcObj.status.latestVersion}"])
            podSelector.untilEach {
                echo "pod: ${it.name()}"
                return it.object().status.containerStatuses[0].ready
            }
        }
    }
}
