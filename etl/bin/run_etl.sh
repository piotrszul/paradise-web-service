#!/bin/bash

#
# Extract Paradise Datase from mysql to to csv files and loads them into a neo4j database.
# 
# Configuration needs to be provided in:
# `$HOME/.paradise.sh`
# 
# The following env variables can be defined:
# `MYSQL_USER` (required) the name of my mysql user to use
# `MYSQL_PWD` (required) mysql password for MYSQL_USER
# `MYSQL_HOST` (optional def=`localhost`) mysql host name
# `NEO4J_HOME` (required) path to neo4j home
# `NEO4J_DB_NAME` (optional) def=`pardise`) the name of neo4j database
# `NEO4J_IMPORT` (optional def=`${NEO4J_HOME}/bin/neo4j-import`) path to the neo-4j import tool
#  NEO4J_DB_FILE` (optional) def=`${NEO4J_HOME}/data/databases/${NEO4J_DB_NAME}.db}`) path to the neo4j database to use 
# `ETL_DIR` (options def = {$PWD}/target) the working directory for the ETL

set  -e 

function info () {
  echo "$1" 1>&2
}

function fatal_error () {
  echo "ERROR: $1" 1>&2
  exit 1
}

function sql_run() {
    mysql -u "${MYSQL_USER}" -h "${MYSQL_HOST}" paradise "$@"
}

function sql_query() {
    sql_run  -N -B -e "$1" 
}

function line_count() {
  echo $(wc -l "$1"  | awk '{print $1}')
}

##
## Bootstreap and confiuration
##

PWD=$(cd `dirname "$0"`/..; pwd)

#
# Common bootstrap should set MYSQL_USERNAME and MYSQL_PASSWORD
#
if [ -f "$HOME/.paradise.sh" ]; then
    echo "Sourcing configuration from : $HOME/.paradise.sh"
    source "$HOME/.paradise.sh"
fi

#
# Process command line opions
#

SUBSET_MAX_ID=
ANONYMIZE=

while [ $# -gt 0 ]; do
    case "$1" in
    --neo4j-db-file)
      shift
      NEO4J_DB_FILE="$1"
      ;;
    --subset--with-max-id)
      shift
      SUBSET_MAX_ID="$1" #e.g: 59094091
      ;;
    --anonymize)
      ANONYMIZE="YES"
      ;;
    -*)
      fatal_error "unrecognized option: $1"
      ;;
    *)
      break;
      ;;
    esac
    shift
done


#
# Validate and mysql configuation
#
MYSQL_USER="${MYSQL_USER:? needs to be set to mysql user}"
MYSQL_PWD="${MYSQL_PWD:? needs to be set to mysql password for MYSQL_USER}"
MYSQL_HOST="${MYSQL_HOST:-localhost}"

#TODO[Securiry] This is not a secure way of passing password. Other options should be considered
# as desctibed here: https://dev.mysql.com/doc/refman/5.5/en/password-security-user.html
export MYSQL_PWD

#
# Validate and complete neo4j configuration
#
NEO4J_HOME="${NEO4J_HOME:? needs to be set to neo4j home}"
NEO4J_DB_NAME="${NEO4J_DB_NAME:-paradise}"
NEO4J_IMPORT="${NEO4J_IMPORT:-${NEO4J_HOME}/bin/neo4j-import}"
NEO4J_DB_FILE="${NEO4J_DB_FILE:-${NEO4J_HOME}/data/databases/${NEO4J_DB_NAME}.db}"

#
# congigure ETL
#
ETL_DIR="${ETL_DIR:-${PWD}/target}"
EXPORT_DIR="${ETL_DIR}/extract"

#
# Prepare ETL dir
#
mkdir -p "${ETL_DIR}"
rm -rf "${ETL_DIR}"

##
## Run extract
##
info "Extracting data from mysql at '${MYSQL_HOST}' with user '${MYSQL_USER}' to: '${EXPORT_DIR}'"

mkdir -p "${EXPORT_DIR}"


#
# Setup extract queries
#

NODE_SUBSET_QUERY=
EDGE_SUBSET_QUERY=
if [ -n "${SUBSET_MAX_ID}" ]; then
  info "Subsetting the extact with max node id = ${SUBSET_MAX_ID}"
  NODE_SUBSET_QUERY="WHERE \`n.node_id\` <= ${SUBSET_MAX_ID}"
  EDGE_SUBSET_QUERY="WHERE node_1 <= ${SUBSET_MAX_ID} AND node_2 <=${SUBSET_MAX_ID}"
fi

ADDRESS_QUERY="\`n.address\`"
NAME_QUERY="\`n.name\`"
if [ -n "${ANONYMIZE}" ]; then
  info "Annonymizing the extract ('name' and 'address' fields)"
  ADDRESS_QUERY="IF(\`n.address\`!='', CONCAT_WS('_','Address',\`n.node_id\`), '') as \`n.address\`"
  NAME_QUERY="IF(\`n.name\`!='', CONCAT_WS('_','Name',\`n.node_id\`), '') as \`n.name\`"
fi

#
# Extract nodes
#
NODE_EXTRACT_FILE="${EXPORT_DIR}/nodes.csv"

MYSQL_NODE_COUNT=$(sql_query "SELECT count(*) FROM (SELECT * from \`nodes.entity\` \
  UNION SELECT * from \`nodes.address\` \
  UNION SELECT * from \`nodes.officer\` \
  UNION SELECT * from \`nodes.intermediary\` \
  UNION SELECT * from \`nodes.other\` \
) as nodes ${NODE_SUBSET_QUERY}")

info "Extracting ${MYSQL_NODE_COUNT} nodes to: '${NODE_EXTRACT_FILE}'"

sql_run <<SQL
SELECT 
    \`n.node_id\`, JSON_UNQUOTE(JSON_EXTRACT(\`labels(n)\`,'\$[0]')),${NAME_QUERY},${ADDRESS_QUERY},
    \`n.valid_until\`,\`n.country_codes\`,\`n.countries\`,\`n.sourceID\`,
    \`n.jurisdiction_description\`,\`n.service_provider\`,\`n.jurisdiction\`,\`n.closed_date\`,
    \`n.incorporation_date\`,\`n.ibcRUC\`,\`n.type\`,\`n.status\`,\`n.company_type\`,\`n.note\`
FROM
   (SELECT * from \`nodes.entity\` 
   UNION SELECT * from \`nodes.address\` 
   UNION SELECT * from \`nodes.officer\` 
   UNION SELECT * from \`nodes.intermediary\` 
   UNION SELECT * from \`nodes.other\` 
   ORDER BY \`n.node_id\`) as nodes ${NODE_SUBSET_QUERY}
INTO OUTFILE '${NODE_EXTRACT_FILE}' 
FIELDS ENCLOSED BY '"' 
TERMINATED BY ',' 
ESCAPED BY '"' 
LINES TERMINATED BY '\n';
SQL


#
# Validate nodes extract
#
CSV_NODE_COUNT=$(line_count "${NODE_EXTRACT_FILE}")

if [[ $CSV_NODE_COUNT == $MYSQL_NODE_COUNT ]]; then
  info "PASS: Counted expected number of lines ${CSV_NODE_COUNT} in: '${NODE_EXTRACT_FILE}'"
else
  fatal_error "FAIL: Counted ${CSV_NODE_COUNT} lines while expected ${MYSQL_NODE_COUNT} in: '${NODE_EXTRACT_FILE}'"
fi


#
# Extract edges (relations)
#
EDGE_EXTRACT_FILE="${EXPORT_DIR}/edges.csv"
MYSQL_EDGE_COUNT=$(sql_query "SELECT count(*) FROM edges ${EDGE_SUBSET_QUERY}")

info "Extracting ${MYSQL_EDGE_COUNT} edges to: '${EDGE_EXTRACT_FILE}'"

sql_run <<SQL
SELECT 
    node_1, node_2, rel_type, idx
FROM
   edges ${EDGE_SUBSET_QUERY}
INTO OUTFILE '${EDGE_EXTRACT_FILE}' 
FIELDS ENCLOSED BY '"' 
TERMINATED BY ',' 
ESCAPED BY '"' 
LINES TERMINATED BY '\n';
SQL

#
# Validate edges extract
#
CSV_EDGE_COUNT=$(line_count "${EDGE_EXTRACT_FILE}")

if [[ $CSV_EDGE_COUNT == $MYSQL_EDGE_COUNT ]]; then
  info "PASS: Counted expected number of lines ${CSV_EDGE_COUNT} in: '${EDGE_EXTRACT_FILE}'"
else
  fatal_error "FAIL: Counted ${CSV_EDGE_COUNT} lines while expected ${MYSQL_EDGE_COUNT} in: '${EDGE_EXTRACT_FILE}'"
fi


##
## Import to Neo4j
##

info "Loading from: '${EXPORT_DIR}' to neo4j db: '${NEO4J_DB_FILE}'"


#
# Load extract
#
NEO4J_IMPORT_LOG="${ETL_DIR}/neo4-import.log"
info "Loading with: '${NEO4J_IMPORT}' output goes to: '${NEO4J_IMPORT_LOG}' ..."

rm -rf "${NEO4J_DB_FILE}"

"${NEO4J_IMPORT}" --into "${NEO4J_DB_FILE}" --id-type=ACTUAL \
  --nodes "${PWD}/res/nodes-header.csv,${NODE_EXTRACT_FILE}" \
  --relationships "${PWD}/res/edges-header.csv,${EDGE_EXTRACT_FILE}" > "${NEO4J_IMPORT_LOG}" 2>&1

#
# Validate loaded nodes
#
NEO4J_NODE_COUNT=$(tail -n 5 "${NEO4J_IMPORT_LOG}" | grep nodes | awk '{print $1}')

if [[ $CSV_NODE_COUNT == $NEO4J_NODE_COUNT ]]; then
  info "PASS: Imported expected number of nodes ${CSV_NODE_COUNT} from: '${NODE_EXTRACT_FILE}'"
else
  fatal_error "FAIL: Imported ${NEO4J_NODE_COUNT} nodes while expected ${CSV_NODE_COUNT} from: '${NODE_EXTRACT_FILE}'"
fi

#
# Validate loaded edges
#
NEO4J_EDGE_COUNT=$(tail -n 5 "${NEO4J_IMPORT_LOG}" | grep relationships | awk '{print $1}')

if [[ $CSV_EDGE_COUNT == $NEO4J_EDGE_COUNT ]]; then
  info "PASS: Imported expected number of edges ${CSV_EDGE_COUNT} from: '${EDGE_EXTRACT_FILE}'"
else
  fatal_error "FAIL: Imported ${NEO4J_EDGE_COUNT} lines while expected ${CSV_EDGE_COUNT} from: '${EDGE_EXTRACT_FILE}'"
fi

info "ETL successfull. Please restart the neo4j instance at: '${NEO4J_HOME}'"


