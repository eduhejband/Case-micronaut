#!/usr/bin/env bash

aws dynamodb create-table \
  --table-name RiskEvents \
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
        {"AttributeName": "customerId", "KeyType": "HASH"},
        {"AttributeName": "processedAt", "KeyType": "RANGE"}
      ],
      "Projection": {"ProjectionType": "ALL"},
      "ProvisionedThroughput": {
        "ReadCapacityUnits": 5,
        "WriteCapacityUnits": 5
      }
    }
  ]' \
  --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
  --endpoint-url http://localhost:8000 \
  --region us-east-1