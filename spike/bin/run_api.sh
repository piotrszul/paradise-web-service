#!/bin/bash

set  -e
PWD=$(cd `dirname "$0"`/..; pwd)


# Common bootstrap should set NEO4J_HOME nad NEO4J_DB_NAME
if [ -f "$HOME/.paradise.sh" ]; then
    echo "Sourcing: $HOME/.paradise.sh"
    source "$HOME/.paradise.sh"
fi

export NEO4J_USERNAME
export NEO4J_PASSWORD

cd "${PWD}/python"

export FLASK_APP="service.py"
flask run

