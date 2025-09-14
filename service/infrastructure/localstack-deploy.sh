#!/bin/bash
set -e # Stops the script if any command fails

STACK_NAME="medilynk"
LOCALSTACK_URL="http://localhost:4566"

echo "Deleting previous log groups..."
LOG_GROUPS=$(aws --endpoint-url=$LOCALSTACK_URL logs describe-log-groups --query "logGroups[].logGroupName" --output text)

for log in $LOG_GROUPS; do
    echo "Deleting log group: $log"
    aws --endpoint-url=$LOCALSTACK_URL logs delete-log-group --log-group-name "$log" || true
done

echo "Deleting CloudFormation stack..."
aws --endpoint-url=$LOCALSTACK_URL cloudformation delete-stack \
    --stack-name $STACK_NAME

echo "Deploying CloudFormation stack..."
aws --endpoint-url=$LOCALSTACK_URL cloudformation deploy \
    --stack-name $STACK_NAME \
    --template-file "./cdk.out/localstack.template.json"

echo "Retrieving load balancer DNS..."
aws --endpoint-url=$LOCALSTACK_URL elbv2 describe-load-balancers \
    --query "LoadBalancers[0].DNSName" --output text
