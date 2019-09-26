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

	openshift.withCluster(input.clusterAPI, input.clusterToken) {
		openshift.withProject(input.projectName) {
			def cmd = input.rollbackVersion?.trim().length() <= 0 ? "" : "--to-revision=${input.rollbackVersion}"

			echo "Attempting to rollback '${input.deploymentConfig}' in ${openshift.project()} ${cmd}"

			def deployment = openshift.selector(input.deploymentConfig)
			deployment.rollout().undo(cmd)
		}
	}
}