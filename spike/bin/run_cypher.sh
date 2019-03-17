#!/bin/bash

set  -e
PWD=$(cd `dirname "$0"`/..; pwd)


# Common bootstrap should set NEO4J_HOME nad NEO4J_DB_NAME
if [ -f "$HOME/.paradise.sh" ]; then
    echo "Sourcing: $HOME/.paradise.sh"
    source "$HOME/.paradise.sh"
fi

NEO4J_HOME="${NEO4J_HOME:? needs to be set to neo4j home}"
NEO4J_CYPHER="${NEO4J_IMPORT:-${NEO4J_HOME}/bin/cypher-shell}"

export NEO4J_USERNAME
export NEO4J_PASSWORD

"${NEO4J_CYPHER}" "$@"

