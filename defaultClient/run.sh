#!/bin/bash

function modoUso() {
    echo "run.sh host port"
    echo "Executes a JaCa-DDM node listening on the port on the interface given by host"
    echo "Arguments:"
    echo "  host: domain or ip address"
    echo "  port: listening tcp-port"
    echo "Example:"
    echo "  run.sh localhost 8080"
}
# Check number of args
[[ $2 ]] || { modoUso; exit 1; }

java  -classpath bin:lib/experimenter.jar defaultClient.Main $1:$2
