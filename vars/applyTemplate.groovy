#!/usr/bin/env groovy

class ApplyTemplateInput implements Serializable {
    //Required
    String templateFile

    //Optional
    String parameterFile

    //Optional - Platform
    String clusterUrl = ""
    String clusterAPI = ""
    String clusterToken = ""
    String projectName = ""
    Integer loglevel = 0
}

def call(Map input) {
    call(new ApplyTemplateInput(input))
} 

def call(ApplyTemplateInput input) {
    assert input.templateFile?.trim() : "Param templateFile should be defined."

    openshift.loglevel(input.loglevel)

    if (input.clusterUrl?.trim().length() > 0) {
        error "clusterUrl is deprecated and will be removed in the next release. Please use 'clusterAPI'"
    }

    openshift.withCluster(input.clusterAPI, input.clusterToken) {
        openshift.withProject(input.projectName) {
            def fileNameArg = input.templateFile.toLowerCase().startsWith("http") ? input.templateFile : "--filename=${input.templateFile}"
            def parameterFileArg = input.parameterFile?.trim()?.length() <= 0 ? "" : "--param-file=${input.parameterFile}"

            echo "Attempting to process template '${fileNameArg}' in ${openshift.project()}"

            def models = openshift.process(fileNameArg, parameterFileArg, "--ignore-unknown-parameters")

            echo "Processed template '${fileNameArg}' will instantiate ${models.size()} objects"

            def created = openshift.apply( models )
            echo "Created: ${created.names()}"
        }
    }
}
