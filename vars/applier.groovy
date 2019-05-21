#!/usr/bin/env groovy

class ApplierInput implements Serializable{
    String secretName = ""
    String registryUrl = ""
    String inventoryPath = ""
    String requirementsPath = ""
    String skipTlsVerify = false
    String rolesPath = "galaxy"
    String applierPlaybook = "galaxy/openshift-applier/playbooks/openshift-cluster-seed.yml"
    String filterTags = ""
}

def call(Map input) {
    call(new ApplierInput(input))
} 

def call(ApplierInput input) {
    assert input.secretName?.trim() : "Param secretName should be defined."
    assert input.registryUrl?.trim() : "Param registryUrl should be defined."
    assert input.inventoryPath?.trim() : "Param inventoryPath should be defined."
    assert input.requirementsPath?.trim() : "Param requirementsPath should be defined."

    script {
        openshift.withCluster() {
            def secretData = openshift.selector("secret/${input.secretName}").object().data
            def encodedToken = secretData.token
            env.TOKEN = sh(script:"set +x; echo ${encodedToken} | base64 --decode", returnStdout: true)
        }

        def fTags = input?.filterTags ? " -e ${input.filterTags} " : ""

        sh """
            oc login ${input.registryUrl} --token=${env.TOKEN} --insecure-skip-tls-verify=${input.skipTlsVerify}
            ansible-galaxy install --role-file=${input.requirementsPath} --roles-path=${input.rolesPath}
            ansible-playbook ${fTags} -i ${input.inventoryPath} ${input.applierPlaybook}
        """
    }
}
