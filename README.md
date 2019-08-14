[![Build Status](https://travis-ci.org/redhat-cop/pipeline-library.svg?branch=master)](https://travis-ci.org/redhat-cop/pipeline-library)
[![License](https://img.shields.io/hexpm/l/plug.svg?maxAge=2592000)]()

# OpenShift Pipeline Library

## What This Repo Is

This is a shared library of Jenkins Pipeline functionality we've developed and use frequently within the CoP. This repo can be imported into a jenkins server (following [this doc](https://jenkins.io/doc/book/pipeline/shared-libraries/#using-libraries)) and used to add functionality to Pipeline scripts.

You can include this repo in your Jenkins Pipeline by defining following at beginning of your Jenkinsfile:

```
library identifier: "pipeline-library@master", retriever: modernSCM(
  [$class: "GitSCMSource",
   remote: "https://github.com/redhat-cop/pipeline-library.git"])
```

Included in this library:

* [org.redhatcop.util.Notifications](./src/org/redhatcop/util/Notifications.txt) - A build status notification system for chat-ops 
* [vars](./vars/) - Many small Jenkins functions for OpenShift


Please see https://github.com/redhat-cop/container-pipelines or https://github.com/redhat-cop/containers-quickstarts for related content.

## Other Resources

* https://jenkins.io/doc/book/pipeline/shared-libraries/
