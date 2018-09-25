# Testing the generic vars

This directory contains a test inventory for testing the generic vars -files in OpenShift. It requires [openshift-applier](https://github.com/redhat-cop/openshift-applier.git) to test. This can be pulled in via ansible-galaxy.

## Dependencies
[redhat-openjdk-18/openjdk18-openshift](https://access.redhat.com/containers/?tab=overview#/registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift) is needed in the `openshift` project to build the test app. Add the image to your `openshift` project: `oc import-image openshift/openjdk18-openshift --from=registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift --confirm`

## Steps to test

1. Pull in dependencies at root of this git repository.
    ```
    ansible-galaxy install -r requirements.yml -p deps
    ```
2. Run ansible under the test directory.
    ```
    ansible-playbook -i test/org/redhatcop/util/end-to-end/inventory/ deps/openshift-applier/playbooks/openshift-cluster-seed.yml
    ```
