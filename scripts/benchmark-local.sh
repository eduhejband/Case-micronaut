#!/usr/bin/env bash

set -e

URL="http://localhost:8080/risk-events"
REQUESTS="${1:-50}"

echo "Running local benchmark with ${REQUESTS} POST requests..."

START_TOTAL=$(date +%s%3N)

for i in $(seq 1 "${REQUESTS}"); do
  START=$(date +%s%3N)

  curl -s -o /dev/null -X POST "${URL}" \
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

  END=$(date +%s%3N)
  DURATION=$((END - START))

  echo "request=${i} duration_ms=${DURATION}"
done

END_TOTAL=$(date +%s%3N)
TOTAL_DURATION=$((END_TOTAL - START_TOTAL))

echo "total_requests=${REQUESTS}"
echo "total_duration_ms=${TOTAL_DURATION}"