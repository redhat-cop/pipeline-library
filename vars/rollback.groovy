#!/usr/bin/env groovy

class Rollback implements Serializable {
	//Required
	String deploymentConfig
	String resourceKindAndName = ""

	//Optional
	String rollbackVersion = ""

	//Optional - Platform
	String clusterUrl = ""
	String clusterAPI = ""
	String clusterToken = ""
	String projectName = ""
	Integer loglevel = 0
}

def call(Map input) {
	call(new Rollback(input))
}

def call(Rollback input) {
	if (input.deploymentConfig?.trim()?.length() > 0) {
		echo "deploymentConfig is deprecated. Please use 'resourceKindAndName'"

		input.resourceKindAndName = input.deploymentConfig
	}
	
	assert input.resourceKindAndName?.trim(): "Param resourceKindAndName should be defined"

	openshift.loglevel(input.loglevel)

	if (input.clusterUrl?.trim().length() > 0) {
		error "clusterUrl is deprecated and will be removed in the next release. Please use 'clusterAPI'"
	}

	openshift.withCluster(input.clusterAPI, input.clusterToken) {
		openshift.withProject(input.projectName) {
			def cmd = input.rollbackVersion?.trim().length() <= 0 ? "" : "--to-revision=${input.rollbackVersion}"

			echo "Attempting to rollback '${input.resourceKindAndName}' in ${openshift.project()} ${cmd}"

			def resource = openshift.selector(input.resourceKindAndName)
			resource.rollout().undo(cmd)
		}
	}
}