#!/usr/bin/env groovy

class ApplyTemplateInput implements Serializable{
    String projectName
    String templateFile
    String parameterFile
    String clusterUrl = ""
    String clusterToken = ""
}

def call(Map input) {
    call(new ApplyTemplateInput(input))
} 

def call(ApplyTemplateInput input) {
    openshift.withCluster(input.clusterUrl, input.clusterToken) {
        openshift.withProject("${input.projectName}") {
            def models = openshift.process( "--filename=${input.templateFile}", "--param-file=${input.parameterFile}", "--ignore-unknown-parameters")
            echo "Creating this template will instantiate ${models.size()} objects"
            
            // We need to find DeploymentConfig definitions inside 
            // So iterating trough all the objects loaded from the Template
            for ( o in models ) {
                if (o.kind == "DeploymentConfig") {
                    // The bug in OCP 3.7 is that when applying DC the "Image" can't be undefined
                    // But when using automatic triggers it updates the value for this on runtime
                    // So when applying this dynamic value gets overwriten and breaks deployments
                    
                    // We will check if this DeploymentConfig already pre-exists and fetch the current value of Image
                    // And set this Image -value into the DeploymentConfig template we are applying
                    def dcSelector = openshift.selector("deploymentconfig/${o.metadata.name}")
                    def foundObjects = dcSelector.exists()
                    if (foundObjects) { 
                    echo "This DC exists, copying the image value"
                    def dcObjs = dcSelector.objects( exportable:true )
                    echo "Image now: ${dcObjs[0].spec.template.spec.containers[0].image}"
                    o.spec.template.spec.containers[0].image = dcObjs[0].spec.template.spec.containers[0].image
                    }
                }
            }
            def created = openshift.apply( models )
            echo "Created: ${created.names()}"
        }
    }
}
