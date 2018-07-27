#!/usr/bin/env groovy

class BinaryBuildInput implements Serializable{
  String projectName
  String buildConfigName
  String binaryDirectoryName

  BinaryBuildInput init(){
    if(!directoryName?.trim()) directoryName = "deploy"
    return this
  }
}

def call (Map input){
     call(new BinaryBuildInput(input).init())
}

def call(BinaryBuildInput input){
  openshift.withCluster() {
    openshift.withProject("${input.project}") {
      openshift.selector("bc", "${input.buildConfigName}").startBuild("--from-dir=${dinput.irectoryName}", "--follow", "--wait")
    }        
  }
}