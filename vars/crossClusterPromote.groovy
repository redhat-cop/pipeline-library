#!/usr/bin/env groovy

class CopyImageInput implements Serializable{
	String sourceImageName
	String sourceImagePath
	String sourceImageTag = "latest"
	String destinationImageName
	String destinationImageTag
	String destinationImagePath
	String targetRegistryCredentials = "other-cluster-credentials"
	String clusterUrl = ""
	String clusterToken = ""

    CopyImageInput init(){
		if(!destinationImageName?.trim()) destinationImageName = sourceImageName
        if(!destinationImageTag?.trim()) destinationImageTag = sourceImageTag
        if(!destinationImagePath?.trim()) destinationImagePath = sourceImagePath
        return this
    }
}

def call(Map input){
	call(new CopyImageInput(input).init())
}

def call(CopyImageInput input) {
	openshift.withCluster(input.clusterUrl, input.clusterToken) {
		def localToken = readFile("/var/run/secrets/kubernetes.io/serviceaccount/token").trim()

		def secretData = openshift.selector("secret/${input.targetRegistryCredentials}").object().data
		def registry = sh(script:"set +x; echo ${secretData.registry} | base64 --decode", returnStdout: true)
		def token = sh(script:"set +x; echo ${secretData.token} | base64 --decode", returnStdout: true)
		def username = sh(script:"set +x; echo ${secretData.username} | base64 --decode", returnStdout: true)

		openshift.withProject("${input.sourceImagePath}") {
			def localRegistry = openshift.selector( "is", "${input.sourceImageName}").object().status.dockerImageRepository
			def from = "docker://${localRegistry}:${input.sourceImageTag}"
			def to = "docker://${registry}/${input.destinationImagePath}/${input.destinationImageName}:${input.destinationImageTag}"

			echo "Now Promoting ${from} -> ${to}"
			sh """
				set +x
				skopeo copy --remove-signatures \
				--src-creds openshift:${localToken} --src-cert-dir=/run/secrets/kubernetes.io/serviceaccount/ \
				--dest-creds ${username}:${token}  --dest-tls-verify=false ${from} ${to}
			"""
		}
	}
}
