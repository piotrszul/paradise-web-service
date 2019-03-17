#!/bin/bash

set  -e
PWD=$(cd `dirname "$0"`/..; pwd)


# Common bootstrap should set NEO4J_HOME nad NEO4J_DB_NAME
if [ -f "$HOME/.paradise.sh" ]; then
    echo "Sourcing: $HOME/.paradise.sh"
    source "$HOME/.paradise.sh"
fi

ETL_DIR=${PWD}/target/etl
EXPORT_DIR=${ETL_DIR}/extract

NEO4J_HOME="${NEO4J_HOME:? needs to be set to neo4j home}"
NEO4J_DB_NAME="${NEO4J_DB_NAME:-paradise}"
NEO4J_IMPORT="${NEO4J_IMPORT:-${NEO4J_HOME}/bin/neo4j-import}"

DEST_DB="${NEO4J_HOME}/data/databases/${NEO4J_DB_NAME}.db"
IMPORT_DIR="${EXPORT_DIR}"

echo "Loading to neo4j db: '${DEST_DB}' from: '${IMPORT_DIR}' using: '${NEO4J_IMPORT}'".

rm -rf "${DEST_DB}"

"${NEO4J_IMPORT}" --into "${DEST_DB}" \
 --nodes:entity "${IMPORT_DIR}/node/entity.csv"

