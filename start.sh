#!/usr/bin/env bash
source profile.sh

if [ $# -eq 0 ]
  then
    echo "No arguments supplied"
    exit
fi
source profile.sh

docker-compose -f $FOLDER/docker-compose-$PROFILE.yml --compatibility start "$@"


