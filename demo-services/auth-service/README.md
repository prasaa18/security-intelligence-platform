# Auth Service (Demo)

**⚠️ DEMO PROJECT FOR SECURITY SCANNING**

This is a demonstration service created to showcase CI/CD security scanning integration with the Security Intelligence Platform.

## Purpose

This service is designed to:
- Demonstrate GitHub Actions integration with security scanning
- Show how security reports are automatically ingested
- Provide realistic security findings for testing

## Security Notice

This is a **DEMO PROJECT** and uses intentionally vulnerable dependency versions for educational purposes only. Do not use this code in production.

## Technology Stack

- Node.js 18
- Express.js
- npm

## Local Development

```bash
npm install
npm start
```

## GitHub Actions Integration

This repository includes a GitHub Actions workflow that:
1. Runs Trivy security scanning on the container image
2. Automatically uploads security reports to the Security Intelligence Platform

### Required GitHub Secrets

Configure these secrets in your GitHub repository settings:

- `SECURITY_INTEL_API_URL`: Your Security Intelligence Platform API URL
- `SCAN_INGESTION_TOKEN`: Your scan ingestion token from the platform
- `SERVICE_NAME`: `auth-service`

### Manual Workflow Trigger

You can manually trigger the security scan workflow from the GitHub Actions tab.

## Security Findings

This demo service uses:
- Older npm packages to generate realistic security findings
- Simple application structure for easy scanning

The findings will be automatically categorized as NEW, UNCHANGED, or NOT_DETECTED_IN_LATEST_SCAN based on scan comparison.