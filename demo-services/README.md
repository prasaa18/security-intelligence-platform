# Demo Services for Security Intelligence Platform

This directory contains demo services designed to showcase CI/CD security scanning integration with the Security Intelligence Platform.

## ⚠️ Important Notice

These are **DEMO PROJECTS** that use intentionally vulnerable dependency versions for educational purposes only. Do not use this code in production environments.

## Available Services

### 1. Payment Service
- **Port:** 3000
- **Purpose:** Demo payment processing service
- **Security Focus:** Vulnerable npm packages for realistic findings

### 2. Order Service  
- **Port:** 3001
- **Purpose:** Demo order management service
- **Security Focus:** Dependency vulnerabilities and encryption demo

### 3. Auth Service
- **Port:** 3002
- **Purpose:** Demo authentication service
- **Security Focus:** JWT and bcrypt implementation with older versions

## Setup Instructions

### Prerequisites
- Node.js 18+
- Docker
- GitHub account with private repository access

### Creating Private GitHub Repositories

For each service:

1. **Create a new private GitHub repository**
   - Go to GitHub and create a new private repository
   - Name it appropriately (e.g., `payment-service-demo`)

2. **Push the demo service code**
   ```bash
   cd payment-service
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
   git push -u origin main
   ```

3. **Configure GitHub Secrets**
   
   Go to your repository Settings → Secrets and variables → Actions → New repository secret
   
   Add the following secrets:
   
   - `SECURITY_INTEL_API_URL`: Your Security Intelligence Platform API URL
     - Example: `http://your-platform.com/api` or `http://localhost:8080/api` (if using tunnel)
   
   - `SCAN_INGESTION_TOKEN`: Your scan ingestion token from the platform
     - Generate a secure token and configure it in your platform's environment variables
   
   - `SERVICE_NAME`: The service name
     - For payment-service: `payment-service`
     - For order-service: `order-service`  
     - For auth-service: `auth-service`

### Local Development

For each service:

```bash
cd payment-service  # or order-service, auth-service
npm install
npm start
```

## GitHub Actions Integration

Each service includes a GitHub Actions workflow that:

1. **Builds the Docker image** for the service
2. **Runs Trivy security scanning** on the container image
3. **Automatically uploads** the security report to the Security Intelligence Platform
4. **Saves the report** as a GitHub Actions artifact

### Triggering Security Scans

**Automatic:**
- Scans run automatically on pushes to `main` or `develop` branches

**Manual:**
- Go to the Actions tab in your GitHub repository
- Select the "Security Scan" workflow
- Click "Run workflow"

### Workflow Process

```
Push to GitHub → Build Docker Image → Run Trivy Scan → 
Upload to Security Intelligence Platform → Save Report as Artifact
```

## Security Report Flow

1. **GitHub Actions** triggers the security scan workflow
2. **Trivy** scans the Docker image and generates a JSON report
3. **Workflow** sends the report to your Security Intelligence Platform via API
4. **Platform** processes the report:
   - Parses and normalizes findings
   - Compares with previous scans (NEW/UNCHANGED/NOT_DETECTED_IN_LATEST_SCAN)
   - Applies service context and prioritization
   - Creates remediation items
5. **Dashboard** shows actionable remediation priorities

## Local Development with GitHub Actions

Since GitHub Actions cannot reach `localhost`, you have two options:

### Option A: Deploy Platform to Reachable Environment
Deploy your Security Intelligence Platform to a cloud environment or server with a public URL.

### Option B: Use a Secure Tunnel (for development)
Use a tunneling service like ngrok to expose your local backend:

```bash
# Install ngrok
# Run ngrok to expose your backend
ngrok http 8080

# Use the ngrok URL as your SECURITY_INTEL_API_URL
# Example: https://abc123.ngrok.io/api
```

## Expected Security Findings

Each demo service is configured to generate realistic security findings:

- **Payment Service:** Vulnerable axios, lodash, moment versions
- **Order Service:** Vulnerable request, uglify-js, node-forge versions  
- **Auth Service:** Vulnerable jsonwebtoken, bcrypt, cookie-parser versions

These findings will be:
- **Classified** by severity (CRITICAL, HIGH, MEDIUM, LOW)
- **Prioritized** based on service context (P0-P4)
- **Compared** with previous scans to detect changes
- **Converted** into actionable remediation items

## Verification

After triggering a GitHub Actions workflow:

1. **Check the workflow status** in the Actions tab
2. **Verify the report upload** in the workflow logs
3. **Check the Security Intelligence Platform dashboard** for new findings
4. **Review the remediation plan** for actionable items

## Troubleshooting

**Workflow fails with authentication error:**
- Verify `SCAN_INGESTION_TOKEN` is correctly set in GitHub Secrets
- Check the token matches what's configured in your platform

**Report not reaching the platform:**
- Verify `SECURITY_INTEL_API_URL` is correct and accessible
- If using localhost, ensure you're using a tunnel
- Check platform logs for incoming requests

**No findings detected:**
- Verify Trivy is running correctly in the workflow
- Check the Trivy report artifact for results
- Ensure the Docker image builds successfully

## Security Best Practices

These demo services violate several security best practices intentionally:

- Using outdated dependencies with known vulnerabilities
- Hardcoded secret keys (for demo purposes only)
- Simplified authentication logic
- No input validation or sanitization

**Never use these patterns in production code.**