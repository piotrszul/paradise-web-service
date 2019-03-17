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

NODES=$(for i in $IMPORT_DIR/node/*.csv; do echo `basename ${i%.*}`; done)

echo "Importig nodes: ${NODES}"

"${NEO4J_IMPORT}" --into "${DEST_DB}" \
  $(for n in $NODES; do echo -n " --nodes ${IMPORT_DIR}/node/${n}.csv"; done) \
  --relationships "${IMPORT_DIR}/edges.csv"

