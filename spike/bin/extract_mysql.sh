#!/bin/bash

set  -e
PWD=$(cd `dirname "$0"`/..; pwd)


# Common bootstrap should set MYSQL_USERNAME and MYSQL_PASSWORD
if [ -f "$HOME/.paradise.sh" ]; then
    echo "Sourcing: $HOME/.paradise.sh"
    source "$HOME/.paradise.sh"
fi

ETL_DIR=${PWD}/target/etl
EXPORT_DIR=${ETL_DIR}/extract

rm -rf ${EXPORT_DIR}
mkdir -p ${EXPORT_DIR}

MYSQL_USER="${MYSQL_USER:? needs to be set to mysql user}"
MYSQL_PASS="${MYSQL_PASS:? needs to be set to mysqp password for MYSQL_USER}"


#
# Extract nodes
#

mkdir -p ${EXPORT_DIR}/node

PROPERTY_FIELDS="valid_until country_codes countries sourceID address name jurisdiction_description service_provider jurisdiction closed_date incorporation_date ibcRUC type status company_type note"


NODES=$(mysql -u crypto -pqwerty paradise -N -B -e "show tables" | grep "nodes" | awk -F "." '{print $2}')
echo "Extrcting nodes: $NODES"

for NODE in ${NODES}; do
   mysql -u ${MYSQL_USER} -p${MYSQL_PASS} paradise <<SQL
SELECT
   'node_id:ID',':LABEL' $(for p in $PROPERTY_FIELDS; do echo -n ",'$p'";done)
UNION ALL
SELECT 
    \`n.node_id\`, JSON_UNQUOTE(JSON_EXTRACT(\`labels(n)\`,'\$[0]')) $(for p in $PROPERTY_FIELDS; do echo -n ",\`n.$p\`";done)
FROM
   \`nodes.${NODE}\`
INTO OUTFILE '${EXPORT_DIR}/node/${NODE}.csv' 
FIELDS ENCLOSED BY '"' 
TERMINATED BY ',' 
ESCAPED BY '"' 
LINES TERMINATED BY '\n';
SQL
done


#
# Extract edges (relations)
#

mysql -u ${MYSQL_USER} -p${MYSQL_PASS} paradise <<SQL
SELECT
   ':START_ID',':END_ID',':TYPE','idx'
UNION ALL
SELECT 
    node_1, node_2, rel_type, idx
FROM
   edges
INTO OUTFILE '${EXPORT_DIR}/edges.csv' 
FIELDS ENCLOSED BY '"' 
TERMINATED BY ',' 
ESCAPED BY '"' 
LINES TERMINATED BY '\n';
SQL

