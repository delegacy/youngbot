#!/usr/bin/env bash

export ANSIBLE_LOG_PATH=$(dirname $0)/ansible-playbook.log.$(date +%Y-%m-%d-%H%M%S)
echo "[$(date +%Y-%m-%dT%H:%M:%S%Z)] ANSIBLE PLAYBOOK: $@" >> $ANSIBLE_LOG_PATH
exec ansible-playbook --diff "$@"
