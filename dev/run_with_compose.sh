#!/bin/bash

#
# Creates an anonymyzed subset extract for integration testing
#
set -e -x
PWD=$(cd `dirname "$0"`/..; pwd)


# Create output dir
DB_SRC_DIR="${PWD}/src/test/neo4j"
DB_RUN_DIR="${PWD}/target/rundb"

rm -rf "${DB_RUN_DIR}"
mkdir -p "${DB_RUN_DIR}"

tar -xzf "${DB_SRC_DIR}/test.db.tar.gz" -C "${DB_RUN_DIR}"

docker-compose -f "${PWD}/dev/docker-compose/docker-compose.yml" "$@"

