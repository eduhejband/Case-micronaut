#!/usr/bin/env bash

set -e

export AWS_ACCESS_KEY_ID=local
export AWS_SECRET_ACCESS_KEY=local
export AWS_DEFAULT_REGION=us-east-1

TABLE_NAME="RiskEvents"
ENDPOINT_URL="http://localhost:8000"
REGION="us-east-1"

echo "Checking DynamoDB table: ${TABLE_NAME}"

if aws dynamodb describe-table \
  --table-name "${TABLE_NAME}" \
  --endpoint-url "${ENDPOINT_URL}" \
  --region "${REGION}" >/dev/null 2>&1; then
  echo "Table ${TABLE_NAME} already exists."
  exit 0
fi

echo "Creating DynamoDB table: ${TABLE_NAME}"

aws dynamodb create-table \
  --table-name "${TABLE_NAME}" \
  --attribute-definitions \
    AttributeName=eventId,AttributeType=S \
    AttributeName=customerId,AttributeType=S \
    AttributeName=processedAt,AttributeType=S \
  --key-schema \
    AttributeName=eventId,KeyType=HASH \
  --global-secondary-indexes '[
    {
      "IndexName": "customerId-processedAt-index",
      "KeySchema": [
        {
          "AttributeName": "customerId",
          "KeyType": "HASH"
        },
        {
          "AttributeName": "processedAt",
          "KeyType": "RANGE"
        }
      ],
      "Projection": {
        "ProjectionType": "ALL"
      },
      "ProvisionedThroughput": {
        "ReadCapacityUnits": 5,
        "WriteCapacityUnits": 5
      }
    }
  ]' \
  --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
  --endpoint-url "${ENDPOINT_URL}" \
  --region "${REGION}"

echo "Table ${TABLE_NAME} created successfully."