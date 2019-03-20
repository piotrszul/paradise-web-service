#!/bin/bash

#
# Runs integration tests 
#
set -e -x
PWD=$(cd `dirname "$0"`/..; pwd)

py.test -v "${PWD}/src/it/tavern/test.api.tavern.yaml"

