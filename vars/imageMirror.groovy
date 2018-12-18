#!/usr/bin/env groovy

class ImageMirrorInput implements Serializable{
    String sourceSecret = ""
    String sourceRegistry = ""
    String destinationSecret = ""
    String destinationRegistry = ""
    String insecure = "false"
    String sourceNamespace = ""
    String destinationNamespace = ""
    String sourceImage = ""
    String destinationImage = ""
    String sourceImageVersion = "latest"
    String destinationImageVersion = "latest"
}

def call(Map input) {
    call(new ImageMirrorInput(input))
} 

def call(ImageMirrorInput input) {

    String sourceApi = input.sourceRegistry.replaceFirst("^(http[s]?://\\.|http[s]?://)","")
    String destinationApi = input.destinationRegistry.replaceFirst("^(http[s]?://\\.|http[s]?://)","")

    assert input.sourceSecret?.trim() : "Param sourceSecret should be defined."
    assert input.sourceRegistry?.trim() : "Param sourceRegistry should be defined."
    assert input.destinationSecret?.trim() : "Param destinationSecret should be defined."
    assert input.destinationRegistry?.trim() : "Param destinationRegistry should be defined."
    assert input.sourceNamespace?.trim() : "Param sourceNamespace should be defined."
    assert input.sourceImage?.trim() : "Param sourceImage should be defined."


    if(input.destinationImage.trim().length() == 0) {
        input.destinationImage = input.sourceImage
    }
    if(input.destinationNamespace.trim().length() == 0) {
        input.destinationNamespace = input.sourceNamespace
    }

    script {
        withDockerRegistry([credentialsId: "${input.sourceSecret}", url: "${input.sourceRegistry}"]) {

            withDockerRegistry([credentialsId: "${input.destinationSecret}", url: "${input.destinationRegistry}"]) {
                sh """
                    oc image mirror --insecure=${input.insecure} \
                        ${sourceApi}/${input.sourceNamespace}/${input.sourceImage}:${input.sourceImageVersion} \
                        ${destinationApi}/${input.destinationNamespace}/${input.destinationImage}:${input.destinationImageVersion}
                   """
            }
        }
    }
}





