#!/bin/bash

#
# Extract Paradise Datase to to csv files
# It assumes the mysql running on `localhost` 
# and data in databse `paradise`.
# 
# Configuration need to be provided in:
# `$HOME/.paradise.sh`
# 
# The following env variables can be defined:
# `MYSQL_USER` (required) the name of my mysql user to use
# `MYSQL_PASS` (required) mysql password for MYSQL_USER
# `MYSQL_HOST` (optional def=`localhost`) mysql host name
# `ETL_DIR` (options def = {$PWD}/target

set  -e
PWD=$(cd `dirname "$0"`/..; pwd)


function info () {
  echo "$1" 1>&2
}

function fatal_error () {
  echo "ERROR: $1" 1>&2
  exit 1
}


# Common bootstrap should set MYSQL_USERNAME and MYSQL_PASSWORD
if [ -f "$HOME/.paradise.sh" ]; then
    echo "Sourcing configuration from : $HOME/.paradise.sh"
    source "$HOME/.paradise.sh"
fi


MYSQL_USER="${MYSQL_USER:? needs to be set to mysql user}"
MYSQL_PASS="${MYSQL_PASS:? needs to be set to mysql password for MYSQL_USER}"
MYSQL_HOST="${MYSQL_HOST:-localhost}"
ETL_DIR="${ETL_DIR:-${PWD}/target}"


EXPORT_DIR="${ETL_DIR}/extract"

mkdir -p "${ETL_DIR}"
rm -rf "${EXPORT_DIR}"
mkdir -p "${EXPORT_DIR}"

#
# Extract nodes
#


NODE_EXTRACT_FILE="${EXPORT_DIR}/nodes.csv"

MYSQL_NODE_COUNT=$(mysql -u crypto -pqwerty paradise -N -B -e "SELECT count(*) FROM (SELECT * from \`nodes.entity\` \
  UNION SELECT * from \`nodes.address\` \
  UNION SELECT * from \`nodes.officer\` \
  UNION SELECT * from \`nodes.intermediary\` \
  UNION SELECT * from \`nodes.other\` \
) as nodes")

info "Extracting ${MYSQL_NODE_COUNT} nodes to: ${NODE_EXTRACT_FILE}"

mysql -u ${MYSQL_USER} -p${MYSQL_PASS} paradise <<SQL
SELECT 
    \`n.node_id\`, JSON_UNQUOTE(JSON_EXTRACT(\`labels(n)\`,'\$[0]')),
    \`n.valid_until\`,\`n.country_codes\`,\`n.countries\`,\`n.sourceID\`,\`n.address\`,
    \`n.name\`,\`n.jurisdiction_description\`,\`n.service_provider\`,\`n.jurisdiction\`,\`n.closed_date\`,
    \`n.incorporation_date\`,\`n.ibcRUC\`,\`n.type\`,\`n.status\`,\`n.company_type\`,\`n.note\`
FROM
   (SELECT * from \`nodes.entity\` 
   UNION SELECT * from \`nodes.address\` 
   UNION SELECT * from \`nodes.officer\` 
   UNION SELECT * from \`nodes.intermediary\` 
   UNION SELECT * from \`nodes.other\` 
   ORDER BY \`n.node_id\`) as nodes
INTO OUTFILE '${NODE_EXTRACT_FILE}' 
FIELDS ENCLOSED BY '"' 
TERMINATED BY ',' 
ESCAPED BY '"' 
LINES TERMINATED BY '\n';
SQL

CSV_NODE_COUNT=$(wc -l "${NODE_EXTRACT_FILE}"  | awk '{print $1}')

if [[ $CSV_NODE_COUNT == $MYSQL_NODE_COUNT ]]; then
  info "PASS: Counted expected number of lines ${CSV_NODE_COUNT} in: ${NODE_EXTRACT_FILE}"
else
  fatal_error "FAIL: Counted ${CSV_NODE_COUNT} lines while expected ${MYSQL_NODE_COUNT} in: ${NODE_EXTRACT_FILE}"
fi


#
# Extract edges (relations)
#

EDGE_EXTRACT_FILE="${EXPORT_DIR}/edges.csv"
MYSQL_EDGE_COUNT=$(mysql -u crypto -pqwerty paradise -N -B -e "SELECT count(*) FROM edges")

info "Extracting ${MYSQL_EDGE_COUNT} edges to: ${EDGE_EXTRACT_FILE}"

mysql -u ${MYSQL_USER} -p${MYSQL_PASS} paradise <<SQL
SELECT 
    node_1, node_2, rel_type, idx
FROM
   edges
INTO OUTFILE '${EDGE_EXTRACT_FILE}' 
FIELDS ENCLOSED BY '"' 
TERMINATED BY ',' 
ESCAPED BY '"' 
LINES TERMINATED BY '\n';
SQL

CSV_EDGE_COUNT=$(wc -l "${EDGE_EXTRACT_FILE}"  | awk '{print $1}')

if [[ $CSV_EDGE_COUNT == $MYSQL_EDGE_COUNT ]]; then
  info "PASS: Counted expected number of lines ${CSV_EDGE_COUNT} in: ${EDGE_EXTRACT_FILE}"
else
  fatal_error "FAIL: Counted ${CSV_EDGE_COUNT} lines while expected ${MYSQL_EDGE_COUNT} in: ${EDGE_EXTRACT_FILE}"
fi

#
# Import to Neo4j
#

NEO4J_HOME="${NEO4J_HOME:? needs to be set to neo4j home}"
NEO4J_DB_NAME="${NEO4J_DB_NAME:-paradise}"
NEO4J_IMPORT="${NEO4J_IMPORT:-${NEO4J_HOME}/bin/neo4j-import}"
NEO4J_BIN="${NEO4J_BIN:-${NEO4J_HOME}/bin/neo4j}"
NEO4J_CYPHER="${NEO4J_CYPHER:-${NEO4J_HOME}/bin/cypher-shell}"

DEST_DB="${NEO4J_HOME}/data/databases/${NEO4J_DB_NAME}.db"
IMPORT_DIR="${EXPORT_DIR}"

NEO4J_IMPORT_LOG="${ETL_DIR}/neo4-import.log"

info "Loading to neo4j db: '${DEST_DB}' from: '${IMPORT_DIR}' using: '${NEO4J_IMPORT}'".

rm -rf "${DEST_DB}"

"${NEO4J_IMPORT}" --into "${DEST_DB}" --id-type=ACTUAL \
  --nodes "${PWD}/res/nodes-header.csv,${IMPORT_DIR}/nodes.csv" \
  --relationships "${PWD}/res/edges-header.csv,${IMPORT_DIR}/edges.csv" | tee "${NEO4J_IMPORT_LOG}"


NEO4J_NODE_COUNT=$(tail -n 5 "${NEO4J_IMPORT_LOG}" | grep nodes | awk '{print $1}')
NEO4J_EDGE_COUNT=$(tail -n 5 "${NEO4J_IMPORT_LOG}" | grep relationships | awk '{print $1}')

info "Imported ${NEO4J_NODE_COUNT} nodes to ${DEST_DB}"
info "Imported ${NEO4J_EDGE_COUNT} edges to ${DEST_DB}"







