#!/usr/bin/env groovy

class SonarQubeConfigurationInput implements Serializable {

    String pomFile = "pom.xml"
    String buildServerWebHookName
    String buildServerWebHookUrl

}

def call(Map input) {
    call(new SonarQubeConfigurationInput(input))
}

def call(SonarQubeConfigurationInput input) {

    // make sure build server web hook is available
    checkForBuildServerWebHook(input)

    // Execute the Maven goal sonar:sonar to attempt to generate
    // the report files.
    withSonarQubeEnv('sonar') {
        try {
            sh 'mvn install sonar:sonar -f ${POM_FILE}'
        } catch (error) {
            error error.getMessage()
        }

    }

    // Check the quality gate to make sure 
    // it is in a passing state.
    def qualitygate = waitForQualityGate()
    if (qualitygate.status != "OK") {
        error "Pipeline aborted due to quality gate failure: ${qualitygate.status}"
    }

}

def checkForBuildServerWebHook(SonarQubeConfigurationInput input) {

    withSonarQubeEnv('sonar') {

        println "Validating webhook with name ${input.buildServerWebHookName} and url ${input.buildServerWebHookUrl} exists..."
        def retVal = sh(returnStatus: true, script: "curl -k -u \"${SONAR_AUTH_TOKEN}:\" http://sonarqube:9000/api/webhooks/list | grep ${input.buildServerWebHookUrl}")
        println "Return Value is $retVal"

        // webhook was not found
        // create the webhook - this should be more likely be part
        // of the sonarqube configuration automation
        if(retVal == 1) {
            println "No webhook found with name ${input.buildServerWebHookName}.  Attempting to create with url ${input.buildServerWebHookUrl}"
            sh "curl -k -X POST -u \"${SONAR_AUTH_TOKEN}:\" -F \"name=${input.buildServerWebHookName}\" -F \"url=${input.buildServerWebHookUrl}\" http://sonarqube:9000/api/webhooks/create"
        }

        // Error happened when trying to find the webhook.  Not sure if it exists
        // so stopping so the pipeline doesn't hang waiting for the quality gate.
        if(retVal == 2) {
            error "Could not determine if the build server webhook was configured.  stopping to avoid hanging while waiting for quality gate."
        }

        println "Build Server Webhook found.  Continuing SonarQube analysis."

    }

}
