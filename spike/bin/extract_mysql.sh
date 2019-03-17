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
mkdir -p ${EXPORT_DIR}/node
mkdir -p ${EXPORT_DIR}/rel

MYSQL_USER="${MYSQL_USER:? needs to be set to mysql user}"
MYSQL_PASS="${MYSQL_PASS:? needs to be set to mysqp password for MYSQL_USER}"


PROPERTY_FIELDS="valid_until country_codes countries sourceID address name jurisdiction_description service_provider jurisdiction closed_date incorporation_date ibcRUC type status company_type note"

mysql -u ${MYSQL_USER} -p${MYSQL_PASS} paradise <<SQL
SELECT
   'entity_id:ID'$(for p in $PROPERTY_FIELDS; do echo -n ",'$p'";done)
UNION
SELECT 
    \`n.node_id\`$(for p in $PROPERTY_FIELDS; do echo -n ",\`n.$p\`";done)
FROM
   \`nodes.entity\`
INTO OUTFILE '${EXPORT_DIR}/node/entity.csv' 
FIELDS ENCLOSED BY '"' 
TERMINATED BY ',' 
ESCAPED BY '"' 
LINES TERMINATED BY '\n';
SQL

