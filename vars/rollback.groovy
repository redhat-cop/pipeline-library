#!/usr/bin/env groovy

class Rollback implements Serializable {
	//Required
	String deploymentConfig

	//Optional
	String rollbackVersion = ""

	//Optional - Platform
	String clusterUrl = ""
	String clusterAPI = ""
	String clusterToken = ""
	String projectName = ""
}

def call(Map input) {
	call(new Rollback(input))
}

def call(Rollback input) {
	assert input.deploymentConfig?.trim(): "Param deploymentConfig should be defined"

	if (input.clusterUrl?.trim().length() > 0) {
		error "clusterUrl is deprecated and will be removed in the next release. Please use 'clusterAPI'"
	}
	
	println "Performing rollback to last successful deployment."

	String rollbackToRevision = ""
	if(input.rollbackVersion.length() > 0) {
		println "Setting revision to rollback to"
		rollbackToRevision = "--to-revision=" + input.rollbackVersion
	}

	openshift.withCluster(input.clusterAPI, input.clusterToken) {
		openshift.withProject(input.projectName) {
			openshift.selector(input.deploymentConfig).rollout().undo(rollbackToRevision)
		}
	}

	println "Finished rollback."
}