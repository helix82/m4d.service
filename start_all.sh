#!/usr/bin/env bash
source profile.sh

export COMPOSE_PARALLEL_LIMIT=1000
docker compose -f $FOLDER/docker-compose-$PROFILE.yml --compatibility up  &
