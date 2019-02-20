#!/usr/bin/env groovy

class SonarQubeConfigurationInput implements Serializable {

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
 
    // Execute the Maven goal sonar:sonar to attempt to generate
    // the report files.
    withSonarQubeEnv('sonarqube') {
        try {
            sh 'mvn install sonar:sonar'
        } catch (error) {
            success = false
            errorMsg = 'Error executing sonar:sonar goal:' + ex.getMessage()
        }
    }

    // Check the quality gate to make sure 
    // it is in a passing state.
    def qualitygate = waitForQualityGate()
    if (success && qualitygate.status != "OK") {
        success = false
        errorMsg = "Pipeline aborted due to quality gate failure: ${qualitygate.status}"
    }

    // publish the dependency check html reports
    dependencyCheckReport(input, success)

    // public the unit test html reports
    unitTestReport()

    // throw error if anything happened
    if(!success) {
        error errorMsg
    }

}

def dependencyCheckReport(SonarQubeConfigurationInput input, boolean success) {

    boolean allowMissing = ${input.dependencyCheckKeepAll}

    if(input.success) {
        allowMissing = false
    }

    report("${input.dependencyCheckReportDir}", "${input.dependencyCheckReportFiles}", "${input.dependencyCheckReportName}", keepAll, ${input.dependencyCheckAlwaysLinkToLastBuild}, ${input.dependencyCheckAllowMissing})

}

def unitTestReport(SonarQubeConfigurationInput input) {

    report("${input.unitTestReportDir}", "${input.unitTestReportFiles}", "${input.unitTestReportName}", ${input.unitTestKeepAll}, ${input.unitTestAlwaysLinkToLastBuild}, ${input.unitTestAllowMissing})

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