#!/usr/bin/env bash
source profile.sh
docker compose -f $FOLDER/docker-compose-$PROFILE.yml down
