#!/usr/bin/env bash

set -e

echo "Stopping API container..."
docker compose stop riskengine-api >/dev/null

echo "Starting API container..."
START=$(date +%s%3N)

docker compose up -d riskengine-api >/dev/null

echo "Waiting for API to respond..."

until curl -s http://localhost:8080/risk-events/health >/dev/null; do
  sleep 0.2
done

END=$(date +%s%3N)
DURATION=$((END - START))

echo "container_startup_until_http_ready_ms=${DURATION}"