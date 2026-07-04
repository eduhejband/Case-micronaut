#!/usr/bin/env bash

set -e

curl -X POST http://localhost:8080/risk-events \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "cus_123",
    "eventType": "PIX_CREATED",
    "amount": 1250.90,
    "deviceId": "dev_abc",
    "channel": "mobile",
    "metadata": {
      "ipRisk": "MEDIUM",
      "country": "BR"
    }
  }'