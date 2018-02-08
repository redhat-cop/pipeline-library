# Testing the org.redhatcop.util package

This directory contains a test inventory for testing the `org.redhatcop.utils` package in OpenShift. It requires [casl-ansible](https://github.com/redhat-cop/casl-ansible.git) to test.

## Steps to test

1. Must create a rocketchat incoming webhook integration
2. Update files/rocketchat-secret.yml to match the webhook integration you created. The secret should look like:
    ```
    apiVersion: v1
    kind: Secret
    metadata:
      name: rocketchat-secret
    stringData:
      server: https://rocketchat.example.com
      key: somehookid
      secret: somehooksecret
    ```
    Where the full rocketchat URL looks like `https://rocketchat.example.com/hooks/somehookid/somehooksecret`.
3. Run ansible.
    ```
    ansible-playbook -i ./inventory/ ~/src/casl-ansible/playbooks/openshift-cluster-seed.yml
    ```
