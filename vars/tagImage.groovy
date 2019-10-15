#!/usr/bin/env groovy

class TagImageInput implements Serializable {
    //Required
    String sourceImagePath =  ""
    String sourceImageName = ""
    String sourceImageTag = "latest"
    String toImagePath

    //Optional
    String toImageName = ""
    String toImageTag = ""

    //Optional - Platform
    String clusterAPI = ""
    String clusterToken = ""
    Integer loglevel = 0

    TagImageInput init() {
        if(!toImageName?.trim()) toImageName = sourceImageName
        if(!toImageTag?.trim()) toImageTag = sourceImageTag
        return this
    }
}

def call(Map input) {
    call(new TagImageInput(input).init())
}

def call(TagImageInput input) {
    assert input.sourceImageName?.trim() : "Param sourceImageName should be defined."
    assert input.sourceImagePath?.trim() : "Param sourceImagePath should be defined."
    assert input.sourceImageTag?.trim() : "Param sourceImageTag should be defined."
    assert input.toImagePath?.trim() : "Param toImagePath should be defined."

    openshift.loglevel(input.loglevel)

    openshift.withCluster(input.clusterAPI, input.clusterToken) {
        def source = "${input.sourceImagePath}/${input.sourceImageName}:${input.sourceImageTag}"
        def destination = "${input.toImagePath}/${input.toImageName}:${input.toImageTag}"

        echo "Attempting to tag; ${source} -> ${destination}"

        openshift.tag(source,destination)
    }
}
