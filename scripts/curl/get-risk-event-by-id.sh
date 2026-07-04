#!/usr/bin/env bash

set -e

EVENT_ID="$1"

if [ -z "${EVENT_ID}" ]; then
  echo "Usage: ./scripts/curl/get-risk-event-by-id.sh <eventId>"
  exit 1
fi

curl -i "http://localhost:8080/risk-events/${EVENT_ID}"