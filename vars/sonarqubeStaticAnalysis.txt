# SonarQube Static Analysis

This function will validate that a build server webhook has been configured in 
SonarQube, run the sonar:sonar maven goal for the configured pom file, and then 
wait for the quality gate status to be OK.  The pipeline will be stopped if the
webhook has not been configured or the quality gate fails. Be advised that the 
SonarQube deployment @ https://github.com/redhat-cop/containers-quickstarts/tree/master/sonarqube
will create this webhook for you.

Sample usage:

Simplest example that assumes that the pom.xml is at the current working directory
and that webhook has been created with the name 'jenkins':
```
stage {
    steps{
        sonarqubeStaticAnalysis()
    }
}
```

Example with all parameters set:
```
stage {
    steps{
        sonarqubeStaticAnalysis(pomFile: "pom.xml",
                                buildServerWebHookName: "jenkins")
    }
}
```

Include this library by adding this before "pipeline" in your Jenkins:
```
library identifier: "pipeline-library@master", retriever: modernSCM(
  [$class: "GitSCMSource",
   remote: "https://github.com/redhat-cop/pipeline-library.git"])
```
