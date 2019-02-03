#!/bin/bash

POSITIONAL=()
while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    #-e|-os)
    #os="$2"
    #shift # past argument
    #shift # past value
    #;;
    --path|-p)
    path="$2"
    shift # past argument
    shift # past value
    ;;
    *)
    POSITIONAL+=("$1") # save it in an array for later
    shift # past argument
    ;;
esac
done
set -- "${POSITIONAL[@]}" # restore positional parameters

if [[ -n $1 ]]; then
    echo "Too many arguments"
else
  if [ -z "$path" ]; then
    path="."
  fi
    echo Using path: "${path}"
    echo
    echo Removing "${path}/Camera/"
    echo
    rm -rf -I "${path}/Camera/"
    echo
    echo Cloning repository into "${path}/Camera"
    git clone -q https://github.com/RMTZuccante/Camera "${path}/Camera"
    chmod +x "${path}/Camera/update.sh"
fi
