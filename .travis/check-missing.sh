#!/usr/bin/env bash

echo "Checking for missing docs and tests"

find vars -type f -name "*.groovy" -exec bash -c '[ -f vars/$(basename {} .groovy).txt ] || echo "Missing vars/$(basename {} .groovy).txt" >> fail.tmp' \;
find vars -type f -name "*.groovy" -exec bash -c '[ -f test/Jenkinsfile-$(basename {} .groovy) ] || echo "Missing test/Jenkinsfile-$(basename {} .groovy)" >> fail.tmp' \;

if [ -f fail.tmp ]; then
    echo "Found:"

    cat fail.tmp
    exit 1
fi