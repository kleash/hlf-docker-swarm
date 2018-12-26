#!/bin/bash

set -ev

ENV_LOCATION=$PWD/.env
echo $ENV_LOCATION
source $ENV_LOCATION

./scripts/network/deploy_services_kafka.sh
./scripts/network/deploy_services_org1.sh
./scripts/network/deploy_services_org2.sh



