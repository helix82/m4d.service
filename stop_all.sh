#!/usr/bin/env bash
source profile.sh
/usr/local/bin/docker-compose -f $FOLDER/docker-compose-$PROFILE.yml down
