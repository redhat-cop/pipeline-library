#!/usr/bin/env groovy

class SonarQubeConfigurationInput implements Serializable {

    String pomFile = "pom.xml"
    String buildServerWebHookName
    String buildServerWebHookUrl
    String dependencyCheckReportDir = 'target'
    String dependencyCheckReportFiles = 'dependency-check-report.html'
    String dependencyCheckReportName = 'OWASP Dependency Check Report'
    boolean dependencyCheckKeepAll = true
    boolean dependencyCheckAlwaysLinkToLastBuild = true
    boolean dependencyCheckAllowMissing = true
    String unitTestReportDir = 'target/site/jacoco/'
    String unitTestReportFiles = 'index.html'
    String unitTestReportName = 'Jacoco Unit Test Report'
    boolean unitTestKeepAll = true
    boolean unitTestAlwaysLinkToLastBuild = false
    boolean unitTestAllowMissing = true

}

def call(Map input) {
    call(new SonarQubeConfigurationInput(input))
}

def call(SonarQubeConfigurationInput input) {

    def success = true
    def errorMsg = ''

    checkForBuildServerWebHook(input)

    // Execute the Maven goal sonar:sonar to attempt to generate
    // the report files.
    withSonarQubeEnv('sonar') {
        try {
            sh 'mvn install sonar:sonar -f ${POM_FILE}'

            sh 'ls -la target'

        } catch (error) {
            success = false
            errorMsg = 'Error executing sonar:sonar goal:' + error.getMessage()
        }

    }

    println "Maven command executed."

    // Check the quality gate to make sure 
    // it is in a passing state.
    def qualitygate = waitForQualityGate()
    if (success && qualitygate.status != "OK") {
        success = false
        errorMsg = "Pipeline aborted due to quality gate failure: ${qualitygate.status}"
    }

    println "Quality Gate checked."

    // publish the dependency check html reports
    dependencyCheckReport(input, success)

    println "Dependency Check Report Generated."

    // public the unit test html reports
    unitTestReport(input)

    println "Unit Test Report Generated."

    // throw error if anything happened
    if(!success) {
        error errorMsg
    }

    println "Done."

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

def dependencyCheckReport(SonarQubeConfigurationInput input, boolean success) {

    boolean allowMissing = input.dependencyCheckKeepAll

    if(success) {
        allowMissing = false
    }

    report(input.dependencyCheckReportDir, input.dependencyCheckReportFiles, input.dependencyCheckReportName, input.dependencyCheckKeepAll, input.dependencyCheckAlwaysLinkToLastBuild, allowMissing)

}

def unitTestReport(SonarQubeConfigurationInput input) {

    report(input.unitTestReportDir, input.unitTestReportFiles, input.unitTestReportName, input.unitTestKeepAll, input.unitTestAlwaysLinkToLastBuild, input.unitTestAllowMissing)

}

def report(String pReportDir, String pReportFiles, String pReportName, boolean pKeepAll, boolean pAlwaysLinkToLastBuild, boolean pAllowMissing) {

    publishHTML(target: [
            reportDir            : pReportDir,
            reportFiles          : pReportFiles,
            reportName           : pReportName,
            keepAll              : pKeepAll,
            alwaysLinkToLastBuild: pAlwaysLinkToLastBuild,
            allowMissing         : pAllowMissing
    ])

}