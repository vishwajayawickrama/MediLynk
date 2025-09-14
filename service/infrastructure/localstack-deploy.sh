#!/bin/bash

set -e  # Stop the script if a command fails

REGION="us-east-1"
AWS_ACCESS_KEY_ID="test"
AWS_SECRET_ACCESS_KEY="test"

# Deploy stack to LocalStack
aws --endpoint-url=http://localhost:4566 cloudformation deploy \
    --stack-name medilynk \
    --template-file "./cdk.out/localstack.template.json" \
    --region $REGION \
    --no-fail-on-empty-changeset \
    --capabilities CAPABILITY_NAMED_IAM \
    --profile default \
    --no-sign-request

# Get DNS name of first load balancer
aws --endpoint-url=http://localhost:4566 elbv2 describe-load-balancers \
    --region $REGION \
    --query 'LoadBalancers[0].DNSName' \
    --output text \
    --no-sign-request
