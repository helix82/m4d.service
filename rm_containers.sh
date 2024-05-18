#!/usr/bin/env bash
source profile.sh
$FOLDER/stop_all.sh
docker rm $(docker ps -a -f status=exited -q)
