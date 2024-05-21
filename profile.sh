#!/usr/bin/env bash
export DOCKER_CLIENT_TIMEOUT=120
export COMPOSE_HTTP_TIMEOUT=120
# Aliases are not expanded when the shell is not interactive, unless the expand_aliases shell option is set using shopt
shopt -s expand_aliases

# common variables:
FOLDER=/root/synapi
PROFILE=local
GIT_REPO_URL=https://git.essocode.ru/o-code/synapi.git
USERNAME=ocode-bot
PASSWORD="changeM3"
