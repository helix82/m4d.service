#!/usr/bin/env bash
source profile.sh

git pull
if [ $# -eq 0 ]
  then
    echo "No arguments supplied"
    exit
fi
source profile.sh

docker-compose -f $FOLDER/docker-compose-$PROFILE.yml up -d --no-deps --build $1
docker-compose -f $FOLDER/docker-compose-$PROFILE.yml restart $1

