# Trivy Integration Guide

## 🎯 Purpose

This guide explains how to integrate Trivy vulnerability scanner with the Security Intelligence Platform for real-time report ingestion and automated security monitoring.

## 📋 Prerequisites

- Trivy scanner installed locally or in CI/CD
- Security Intelligence Platform running (local or deployed)
- Valid SCAN_INGESTION_TOKEN configured in the platform
- Service registered in the platform

## 🔧 Integration Methods

### Method 1: Manual Upload via Web UI

1. Navigate to the Reports page in the platform
2. Click "Upload Security Report"
3. Select your Trivy JSON report file
4. Enter service name and environment
5. Click "Upload"

### Method 2: API Upload (REST)

**Endpoint:** `POST /api/reports/upload`

**Parameters:**
- `file`: Trivy JSON report file (multipart/form-data)
- `serviceName`: Name of the service being scanned
- `environment`: Environment (PRODUCTION/DEVELOPMENT/STAGING)

**Example:**
```bash
curl -X POST http://localhost:8080/api/reports/upload \
  -F "file=@trivy-report.json" \
  -F "serviceName=payment-service" \
  -F "environment=PRODUCTION"
```

**Response:**
```json
{
  "success": true,
  "message": "Report processed successfully",
  "tool": "TRIVY",
  "serviceName": "payment-service",
  "rawFindings": 15,
  "uniqueFindings": 12,
  "report": {
    "id": "...",
    "serviceName": "payment-service",
    "tool": "TRIVY",
    "status": "SUCCESS"
  }
}
```

### Method 3: GitHub Actions Integration (Recommended for CI/CD)

**Endpoint:** `POST /api/integrations/scans/github-actions`

**Authentication:** Bearer token in Authorization header

**Parameters:**
- `file`: Trivy JSON report file (multipart/form-data)
- `serviceName`: Service name
- `repository`: GitHub repository (e.g., "org/repo")
- `branch`: Branch name (e.g., "main")
- `commitId`: Git commit SHA
- `workflowRunId`: GitHub Actions workflow run ID
- `tool`: Scanner tool (TRIVY)
- `scanType`: Scan type (CONTAINER/FILESYSTEM/DEPENDENCY)
- `environment`: Environment (default: PRODUCTION)

**Example GitHub Actions Workflow:**

```yaml
name: Security Scan with Trivy

on:
  push:
    branches: [ main, develop ]
  workflow_dispatch:

jobs:
  security-scan:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
    
    - name: Build Docker image
      run: docker build -t my-service:latest .
    
    - name: Run Trivy vulnerability scanner
      uses: aquasecurity/trivy-action@master
      with:
        image-ref: 'my-service:latest'
        format: 'json'
        output: 'trivy-report.json'
    
    - name: Upload to Security Intelligence Platform
      run: |
        curl -X POST "${{ secrets.SECURITY_INTEL_API_URL }}/api/integrations/scans/github-actions" \
          -H "Authorization: Bearer ${{ secrets.SCAN_INGESTION_TOKEN }}" \
          -F "file=@trivy-report.json" \
          -F "serviceName=my-service" \
          -F "repository=${{ github.repository }}" \
          -F "branch=${{ github.ref_name }}" \
          -F "commitId=${{ github.sha }}" \
          -F "workflowRunId=${{ github.run_id }}" \
          -F "tool=TRIVY" \
          -F "scanType=CONTAINER" \
          -F "environment=PRODUCTION"
```

**Required GitHub Secrets:**
- `SECURITY_INTEL_API_URL`: Your platform API URL (e.g., `https://your-platform.com/api`)
- `SCAN_INGESTION_TOKEN`: Your configured ingestion token from backend `.env`

## 🚀 Local Development with Trivy

### Running Trivy Locally

**Scan a Docker image:**
```bash
trivy image --format json --output trivy-report.json my-service:latest
```

**Scan a filesystem:**
```bash
trivy fs --format json --output trivy-report.json ./my-project
```

**Scan dependencies:**
```bash
trivy repo --format json --output trivy-report.json ./my-project
```

### Uploading Local Reports

```bash
# After generating trivy-report.json
curl -X POST http://localhost:8080/api/reports/upload \
  -F "file=@trivy-report.json" \
  -F "serviceName=my-local-service" \
  -F "environment=DEVELOPMENT"
```

## 🔍 Testing Your Integration

### 1. Test Manual Upload
```bash
# Generate a sample Trivy report
trivy image --format json --output test-report.json alpine:latest

# Upload to platform
curl -X POST http://localhost:8080/api/reports/upload \
  -F "file=@test-report.json" \
  -F "serviceName=test-service" \
  -F "environment=DEVELOPMENT"
```

### 2. Test GitHub Actions Integration
```bash
# Test the health endpoint
curl -X GET http://localhost:8080/api/integrations/scans/github-actions/health

# Expected response: "GitHub Actions integration is ready"
```

### 3. Verify Dashboard
After uploading a report:
1. Navigate to the Dashboard
2. Check "Security Action Center" for new findings
3. Verify the service appears in "Recent Scan Activity"
4. Check that findings are prioritized correctly

## 📊 Expected Behavior

### What Happens After Upload

1. **Report Ingestion**: Platform parses the Trivy JSON report
2. **Normalization**: Findings are converted to unified format
3. **Deduplication**: Duplicate findings across tools are correlated
4. **Prioritization**: Risk scores and priorities (P0-P4) are calculated
5. **Scan Comparison**: Compared with previous scans to detect changes
6. **Remediation Items**: Actionable remediation items are created
7. **Dashboard Update**: Action Center and metrics are updated

### Finding Classification

- **NEW**: Vulnerabilities detected in current scan but not in previous scan
- **UNCHANGED**: Vulnerabilities present in both current and previous scans
- **NOT_DETECTED_IN_LATEST_SCAN**: Vulnerabilities from previous scan not found in current scan

## 🛠️ Troubleshooting

### Common Issues

**Issue:** "SCAN_INGESTION_TOKEN not configured"
- **Solution:** Set `SCAN_INGESTION_TOKEN` in backend `.env` file and restart server

**Issue:** "Failed to parse security report"
- **Solution:** Ensure Trivy report is valid JSON format
- **Check:** Run `trivy image --format json` to generate correct format

**Issue:** "Invalid authentication token"
- **Solution:** Verify Bearer token matches `SCAN_INGESTION_TOKEN` exactly
- **Check:** Ensure Authorization header format: `Bearer YOUR_TOKEN`

**Issue:** "Service not found"
- **Solution:** Register the service in the platform first via Services page

**Issue:** GitHub Actions cannot reach localhost
- **Solution:** Use ngrok or similar tunnel for local development
- **Command:** `ngrok http 8080` then use ngrok URL as `SECURITY_INTEL_API_URL`

## 📈 Best Practices

1. **Automate Scans**: Integrate Trivy into CI/CD pipeline for every build
2. **Scan Frequency**: 
   - Production: Daily or on every deployment
   - Development: Weekly or on every PR
3. **Scan Types**: Use appropriate scan type for your use case
   - CONTAINER: For Docker images
   - FILESYSTEM: For source code
   - DEPENDENCY: For package vulnerabilities
4. **Environment Labeling**: Always specify correct environment for accurate prioritization
5. **Service Naming**: Use consistent service names across scans for proper tracking

## 🔗 Related Documentation

- [README.md](README.md) - Main platform documentation
- [BUSINESS_GOALS.md](BUSINESS_GOALS.md) - Business objectives and requirements
- [GITHUB_SETUP.md](GITHUB_SETUP.md) - GitHub Actions setup instructions
- [QUICK_START.md](QUICK_START.md) - Quick start guide

## 💡 Example Use Cases

### Use Case 1: Microservices Security Monitoring

```yaml
# Scan multiple microservices
services:
  - payment-service
  - order-service  
  - auth-service

for service in "${services[@]}"; do
  docker build -t $service:latest ./services/$service
  trivy image --format json --output $service-trivy.json $service:latest
  curl -X POST http://platform/api/reports/upload \
    -F "file=@$service-trivy.json" \
    -F "serviceName=$service" \
    -F "environment=PRODUCTION"
done
```

### Use Case 2: PR Security Checks

```yaml
# In GitHub Actions for pull requests
- name: Security Scan
  if: github.event_name == 'pull_request'
  run: |
    trivy fs --format json --output pr-scan.json .
    curl -X POST ${{ secrets.SECURITY_INTEL_API_URL }}/api/reports/upload \
      -H "Authorization: Bearer ${{ secrets.SCAN_INGESTION_TOKEN }}" \
      -F "file=@pr-scan.json" \
      -F "serviceName=${{ github.repository }}" \
      -F "environment=DEVELOPMENT"
```

### Use Case 3: Scheduled Security Scans

```yaml
# GitHub Actions scheduled workflow
on:
  schedule:
    - cron: '0 2 * * *' # Daily at 2 AM

jobs:
  nightly-security-scan:
    runs-on: ubuntu-latest
    steps:
      # Checkout, build, scan, and upload...
```

## 🎯 Success Indicators

Your Trivy integration is successful when:

✅ Trivy reports upload without errors
✅ Findings appear in the dashboard within seconds
✅ Priorities (P0-P4) are assigned based on business context
✅ Scan comparison shows NEW/UNCHANGED/RESOLVED classifications
✅ AI assistant can provide remediation guidance for findings
✅ Teams can track remediation progress in the platform