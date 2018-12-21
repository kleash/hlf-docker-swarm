#!/bin/bash

ENV_LOCATION=$PWD/.env
echo $ENV_LOCATION
source $ENV_LOCATION

docker network create --driver overlay --attachable "$NETWORK_NAME"