#!/usr/bin/env bash

set -e

export AWS_ACCESS_KEY_ID=local
export AWS_SECRET_ACCESS_KEY=local
export AWS_DEFAULT_REGION=us-east-1

TABLE_NAME="RiskEvents"
ENDPOINT_URL="http://localhost:8000"
REGION="us-east-1"

aws dynamodb scan \
  --table-name "${TABLE_NAME}" \
  --endpoint-url "${ENDPOINT_URL}" \
  --region "${REGION}"