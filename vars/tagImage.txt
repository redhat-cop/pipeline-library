# Tag Image

Easiest way to copy images between OpenShift projects is to use imagestreams tags.
This function will issue this tag from source ImageStream to target ImageStream.

Sample usage:

Simplest example that assumes Image tag to be "latest" and destination Image name plus tag to be same as with source:
```
stage {
    steps{
        tagImage(sourceImageName: "hello-world", sourceImagePath: "example-project")
    }
}
```

Example with all parameters set:
```
stage {
    steps{
        tagImage(sourceImageName: "hello-world", 
                 sourceImagePath: "example-project", 
                 sourceImageTag: "test-ready", 
                 toImageName: "hello-testers", 
                 toImagePath: "test-project",
                 toImageTag: "test")
    }
}
```

Include this library by adding this before "pipeline" in your Jenkins:
```
library identifier: "pipeline-library@master", retriever: modernSCM(
  [$class: "GitSCMSource",
   remote: "https://github.com/redhat-cop/pipeline-library.git"])
```