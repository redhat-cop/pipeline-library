#!/usr/bin/env groovy

class SonarQubeConfigurationInput implements Serializable {

    String pomFile = "pom.xml"
    String buildServerWebHookName = "jenkins"
    String curlOptions = ""

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
            sh "mvn sonar:sonar -f ${input.pomFile}"
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

        println "Validating webhook with name ${input.buildServerWebHookName} exists..."
        def retVal = sh(returnStdout: true, script: "curl ${input.curlOptions} -u '${SONAR_AUTH_TOKEN}:' ${SONAR_HOST_URL}/api/webhooks/list")
        println "Return Value is $retVal"

        def tmpfile = "/tmp/sonarwebhooks-${java.util.UUID.randomUUID()}.json"
        writeFile file: tmpfile, text: retVal
        
        def webhooksObj = readJSON file: tmpfile
        def foundHook = webhooksObj?.webhooks?.find { it.name.equalsIgnoreCase(input.buildServerWebHookName) }

        // webhook was not found
        // create the webhook - this should be more likely be part
        // of the sonarqube configuration automation
        if(foundHook == null) {
            error "No webhook found with name ${input.buildServerWebHookName}.  Please create one in SonarQube."
        }

        println "Build Server Webhook found.  Continuing SonarQube analysis."

    }

}
