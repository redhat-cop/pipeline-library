#!/usr/bin/env groovy

class Rollback implements Serializable {
	String deploymentConfig
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
	if (input.clusterUrl?.trim().length() > 0) {
		echo "WARNING: clusterUrl is deprecated. Please use 'clusterAPI'"

		input.clusterAPI = input.clusterUrl
	}
	
	println "Performing rollback to last successful deployment."

	String rollbackToRevision = ""
	if(input.rollbackVersion.length() > 0) {
		println "Setting revision to rollback to"
		rollbackToRevision = "--to-revision=" + input.rollbackVersion
	}

	openshift.withCluster(input.clusterUrl, input.clusterToken) {
		openshift.withProject(input.projectName) {
			openshift.selector(input.deploymentConfig).rollout().undo(rollbackToRevision)
		}
	}

	println "Finished rollback."
}