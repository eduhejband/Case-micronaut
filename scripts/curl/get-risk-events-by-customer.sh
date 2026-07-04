#!/usr/bin/env bash

set -e

CUSTOMER_ID="${1:-cus_123}"

curl -i "http://localhost:8080/risk-events/customer/${CUSTOMER_ID}"