#!/usr/bin/env bash
# ==============================================================================
# Security Intelligence Platform — Universal Scan Ingestion Script
# Usage:
#   ./scripts/ingest-scan.sh <report-file.json> <service-name> [environment] [platform-url]
# Example:
#   ./scripts/ingest-scan.sh ./trivy-results.json payment-service PRODUCTION https://sec-intel.myorg.com
# ==============================================================================

set -e

REPORT_FILE="$1"
SERVICE_NAME="$2"
ENVIRONMENT="${3:-PRODUCTION}"
PLATFORM_URL="${4:-${SECURITY_PLATFORM_URL:-http://localhost:8080}}"

if [ -z "$REPORT_FILE" ] || [ -z "$SERVICE_NAME" ]; then
  echo "Error: Missing arguments."
  echo "Usage: $0 <report-file.json> <service-name> [environment] [platform-url]"
  exit 1
fi

if [ ! -f "$REPORT_FILE" ]; then
  echo "Error: Report file '$REPORT_FILE' does not exist."
  exit 1
fi

echo "========================================================"
echo "🛡 Security Intelligence Platform — Ingesting Scan"
echo "  File:         $REPORT_FILE"
echo "  Service:      $SERVICE_NAME"
echo "  Environment:  $ENVIRONMENT"
echo "  Endpoint:     $PLATFORM_URL/api/reports/upload"
echo "========================================================"

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$PLATFORM_URL/api/reports/upload" \
  -F "file=@$REPORT_FILE" \
  -F "serviceName=$SERVICE_NAME" \
  -F "environment=$ENVIRONMENT")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -ge 200 ] && [ "$HTTP_CODE" -lt 300 ]; then
  echo "✓ Scan ingested successfully! (HTTP $HTTP_CODE)"
  echo "$BODY"
else
  echo "✗ Ingestion failed with HTTP $HTTP_CODE"
  echo "$BODY"
  exit 1
fi

