#!/usr/bin/env bash

# update an AWS ECS service with the new image
aws ecs update-service --cluster "cluster-young-bot-server" \
                       --service "service-young-bot-server" \
                       --task-definition "td-young-bot-server" \
                       --force-new-deployment
