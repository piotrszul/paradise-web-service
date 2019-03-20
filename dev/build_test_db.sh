#!/bin/bash

#
# Creates an anonymyzed subset extract for integration testing
#
set -e -x
PWD=$(cd `dirname "$0"`/..; pwd)

# Create anonmized extract
${PWD}/etl/bin/run_etl.sh --subset--with-max-id 59094091 --anonymize --neo4j-db-file "${PWD}/target/testdb/test.db"

# Create output dir
DEST_DIR="${PWD}/src/test/neo4j"
mkdir -p "${DEST_DIR}"

tar -czf "${DEST_DIR}/test.db.tar.gz" -C "${PWD}/target/testdb" test.db
