# Security Remediation Intelligence Platform

A centralized security intelligence platform that transforms scanner findings into **clear, actionable remediation decisions**. The platform ingests reports from vulnerability scanners (Trivy, Snyk), normalizes findings, correlates duplicates across tools, applies service business context, calculates remediation priorities (P0–P4), and provides AI-assisted guidance for engineering teams.

**🎯 Business Goal:** Convert scanner findings into actionable decisions - "What should we fix first? Why? Who owns it? What should they do?"

---

## 🎯 1. Product Purpose

Organizations already utilize various security scanning tools:
- **Container / OS Scanning:** Trivy
- **Software Composition Analysis (SCA) & Dependencies:** Snyk, Trivy
- **SAST / DAST / API Scanners:** SonarQube, Fortify, OWASP ZAP, 42Crunch

The **Security Remediation Intelligence Platform** does NOT scan code itself; it **ingests**, **normalizes**, **deduplicates**, **prioritizes**, and **provides actionable guidance** for scanner reports based on realistic service context.

### Core Questions Answered
1. **What should we fix first?** - Prioritized remediation plan (P0-P4)
2. **Why should we fix it first?** - Risk scoring with business context
3. **Which team owns it?** - Service ownership and team assignment
4. **What action should they take?** - Recommended remediation steps
5. **What is the latest security state?** - Service security status (HEALTHY/ATTENTION/CRITICAL/STALE)
6. **Are our scans fresh or stale?** - Scan freshness monitoring
7. **What changed since the previous scan?** - NEW/UNCHANGED/NOT_DETECTED_IN_LATEST_SCAN classification
8. **What should engineering focus on today?** - Action Center dashboard
9. **How can Gemini help explain and guide remediation?** - AI-assisted security guidance

### Core Processing Flow
```
SCANNER REPORT (Trivy / Snyk JSON)
      ↓
INGEST (Multipart upload or GitHub Actions)
      ↓
SCAN EXECUTION CREATION
      ↓
NORMALIZE (Unified SecurityFinding model & fingerprinting)
      ↓
COMPARE WITH PREVIOUS SCAN (NEW/UNCHANGED/NOT_DETECTED_IN_LATEST_SCAN)
      ↓
DEDUPLICATE (Deterministic correlation across tools)
      ↓
SERVICE CONTEXT (Criticality, Exposure, Data Sensitivity, Env)
      ↓
DETERMINISTIC PRIORITIZATION ENGINE
      ↓
REMEDIATION ITEM CREATION
      ↓
SECURITY STATE CALCULATION
      ↓
AI-ASSISTED GUIDANCE (Gemini)
      ↓
ACTIONABLE REMEDIATION DECISIONS
```

---

## 🏗️ 2. Architecture & Tech Stack

### Technology Stack
- **Backend:** Java 20 / 21, Spring Boot 3.2.0, Spring Data MongoDB, Bean Validation, JUnit 5, Mockito, Jackson
- **Frontend:** Angular 17 (Standalone Components, TypeScript, Reactive CSS UI)
- **Database:** MongoDB 7.0 (Collections: `scan_executions`, `security_findings`, `remediation_items`, `services`)
- **AI Integration:** Google Gemini API for contextual security guidance
- **CI/CD Integration:** GitHub Actions for automated security scanning
- **Containerization:** Docker Compose

### New Architecture Components

#### Backend Enhancements
- **RemediationItem Domain:** Actionable remediation units with status tracking
- **Scan Execution:** Point-in-time security results with comparison
- **Security State Calculation:** HEALTHY/ATTENTION/CRITICAL/STALE/UNKNOWN states
- **Scan Comparison Engine:** NEW/UNCHANGED/NOT_DETECTED_IN_LATEST_SCAN classification
- **GitHub Actions Integration:** Automated CI/CD report ingestion
- **Gemini AI Service:** Contextual security guidance and explanations
- **Action Center Dashboard:** Prioritized view of what needs attention today

#### New Entities
- `ScanExecution` - Individual scan runs with metadata and results
- `RemediationItem` - Actionable remediation units with lifecycle management
- `DetectionState` - NEW/PRESENT/NOT_DETECTED_IN_LATEST_SCAN tracking
- `SecurityState` - HEALTHY/ATTENTION/CRITICAL/STALE/UNKNOWN per service
- `TriggerType` - MANUAL_UPLOAD/GITHUB_ACTIONS/API

---

## 📁 3. Project Structure

```
security-intelligence-platform/
├── backend/
│   ├── pom.xml
│   ├── .env.example
│   └── src/
│       ├── main/
│       │   ├── java/com/securityintel/
│       │   │   ├── ai/                  # GeminiAiService
│       │   │   ├── comparison/           # ScanComparisonEngine
│       │   │   ├── config/               # MongoConfig, RestTemplateConfig
│       │   │   ├── controller/           # Dashboard, Remediation, Scans, AI Assistant, GitHub Actions Integration
│       │   │   ├── deduplication/        # DeduplicationEngine, DeduplicationResult
│       │   │   ├── dto/                  # DashboardSummaryDto, ScanReportDto, SecurityFindingDto, ServiceDto
│       │   │   ├── exception/            # GlobalExceptionHandler, Custom Exceptions
│       │   │   ├── mapper/               # EntityMapper
│       │   │   ├── model/                # Domain entities (SecurityFinding, Service, ScanExecution, RemediationItem, Enums)
│       │   │   ├── normalization/        # FindingNormalizer
│       │   │   ├── parser/               # SecurityReportParser, TrivyReportParser, SnykReportParser, Factory
│       │   │   ├── prioritization/       # SecurityPrioritizationEngine, PriorityResult
│       │   │   ├── remediation/          # RemediationService
│       │   │   ├── repository/           # ScanExecutionRepository, RemediationItemRepository, SecurityFindingRepository, ServiceRepository
│       │   │   ├── scan/                 # ScanExecutionService
│       │   │   ├── securitystate/        # SecurityStateCalculator
│       │   │   ├── service/              # DashboardService, SecurityReportService, SecurityFindingService, ServiceManagementService
│       │   │   └── SecurityIntelligencePlatformApplication.java
│       │   └── resources/
│       │       └── application.yml
│       └── test/                         # Comprehensive JUnit 5 & Mockito test suite
├── frontend/
│   ├── angular.json
│   ├── package.json
│   └── src/
│       ├── app/
│       │   ├── models/                   # dashboard.model.ts
│       │   ├── pages/
│       │   │   ├── dashboard/            # Security Action Center, Top Remediation Plan, AI Brief
│       │   │   ├── remediation/          # Remediation Plan page with filters and status updates
│       │   │   ├── findings/             # Filterable & searchable findings table
│       │   │   ├── finding-detail/       # Detailed finding view + Ask Gemini features
│       │   │   ├── scans/                # Scan history and comparison
│       │   │   ├── services/             # Service catalog with security state
│       │   │   └── ai-assistant/         # Context-aware AI security assistant
│       │   ├── services/                 # api.service.ts
│       │   ├── app.component.ts
│       │   └── app.routes.ts
│       └── styles.css
├── demo-services/                        # Demo services for GitHub Actions integration
│   ├── payment-service/                 # Demo payment service with vulnerable dependencies
│   ├── order-service/                   # Demo order service with vulnerable dependencies
│   ├── auth-service/                    # Demo auth service with vulnerable dependencies
│   └── README.md                        # Demo services setup guide
├── sample-data/
│   ├── sample-trivy-report.json          # Trivy container & package scan sample
│   ├── sample-snyk-report.json           # Snyk vulnerability report sample
│   └── sample-trivy-order-service.json   # Trivy report for order-service
├── mongodb-init/
│   └── init.js                          # MongoDB seed script
├── docker-compose.yml
└── README.md
```

---

## ⚡ 4. How to Start Locally

### Prerequisites
- Java 20 or 21
- Node.js 18+
- MongoDB 7.0 (or use Docker Compose)
- (Optional) Google Gemini API key for AI features

### Environment Configuration

1. **Copy the example environment file:**
   ```bash
   cd backend
   cp .env.example .env
   ```

2. **Configure the following variables in `.env`:**
   ```bash
   MONGODB_URI=mongodb+srv://admin:admin@cluster0.hs3mybp.mongodb.net/securityintel
   SCAN_INGESTION_TOKEN=your-secret-token-here
   GEMINI_API_KEY=your-gemini-api-key-here  # Optional but recommended
   ```

### Option A: MongoDB with Docker Compose + Run Apps Locally

1. **Start MongoDB:**
   ```bash
   docker compose up -d mongodb
   ```

2. **Start Spring Boot Backend:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   *Backend runs on `http://localhost:8080/api`*

3. **Start Angular Frontend:**
   ```bash
   cd frontend
   npm start
   ```
   *Frontend UI runs on `http://localhost:4200`*

---

## 🔁 5. How Scan Comparison Works

The platform automatically compares each new scan with the previous successful scan for the same service, tool, and scan type.

### Classification Logic

**NEW Findings:**
- Exist in the latest scan but not in the previous scan
- Indicate newly introduced vulnerabilities
- Set `detectionState = NEW`
- Update `firstDetectedAt` and `lastDetectedAt`

**UNCHANGED Findings:**
- Exist in both the current and previous scans
- Indicate persistent vulnerabilities
- Set `detectionState = PRESENT`
- Preserve `firstDetectedAt`, update `lastDetectedAt`

**NOT_DETECTED_IN_LATEST_SCAN Findings:**
- Existed in the previous scan but not in the latest scan
- Indicate potential fixes or configuration changes
- Set `detectionState = NOT_DETECTED_IN_LATEST_SCAN`
- **Important:** Not automatically marked as RESOLVED - requires explicit remediation status update

### Comparison Process
```
NEW SCAN ARRIVES
      ↓
FIND PREVIOUS SUCCESSFUL SCAN (same service, tool, scan type)
      ↓
COMPARE FINDINGS BY FINGERPRINT
      ↓
CLASSIFY AS NEW/UNCHANGED/NOT_DETECTED_IN_LATEST_SCAN
      ↓
UPDATE FINDING DETECTION STATES
      ↓
CREATE/UPDATE REMEDIATION ITEMS
      ↓
CALCULATE SCAN STATISTICS (new/resolved/unchanged counts)
```

---

## 🔐 6. GitHub Actions Integration

The platform provides a secure endpoint for automated CI/CD security scanning integration.

### Integration Endpoint
```
POST /api/integrations/scans/github-actions
```

### Authentication
- Uses `SCAN_INGESTION_TOKEN` environment variable
- Requires `Authorization: Bearer {token}` header
- Validates token before processing any reports

### GitHub Actions Workflow Example

```yaml
name: Security Scan

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

### Required GitHub Secrets
- `SECURITY_INTEL_API_URL`: Your platform API URL
- `SCAN_INGESTION_TOKEN`: Your configured ingestion token
- `SERVICE_NAME`: The service name to associate with scans

### Local Development with GitHub Actions
Since GitHub Actions cannot reach `localhost`, use a secure tunnel for development:
```bash
# Using ngrok
ngrok http 8080
# Use the ngrok URL as SECURITY_INTEL_API_URL
```

---

## 🤖 7. Gemini AI Integration

The platform integrates Google Gemini API to provide contextual security guidance while maintaining the deterministic engine as the source of truth.

### AI Principles
- **Deterministic Engine Remains Authoritative:** Risk scores, priorities, CVSS, and fixed versions come from the scanner/system
- **Gemini Provides Explanations:** AI explains context, suggests remediation steps, and provides guidance
- **Clear Fact/Guidance Separation:** UI clearly distinguishes between system facts and AI guidance
- **No AI Invention:** Gemini cannot invent facts - only explains provided context

### AI Features

#### 1. Explain Priority
Explains why a finding received its priority level based on:
- Technical severity and CVSS score
- Service context (environment, criticality, exposure)
- Business impact factors

#### 2. Remediation Guidance
Provides step-by-step remediation instructions:
- How to fix the vulnerability
- Verification steps
- Potential risks and assumptions
- Additional security considerations

#### 3. Service Risk Summary
Summarizes the security posture of a service:
- Overall security assessment
- Top risks requiring attention
- Recommended actions for the service team

#### 4. Daily Security Brief
Generates an executive summary of security priorities:
- What requires attention today
- Which services need focus
- Recent security changes
- Recommended priorities for engineering teams

### Configuration
```bash
# In backend/.env
GEMINI_API_KEY=your-gemini-api-key-here
GEMINI_MODEL=gemini-1.5-flash
```

### API Endpoints
- `GET /api/ai-assistant/configured` - Check if AI is configured
- `POST /api/ai-assistant/explain-priority` - Explain priority for a finding
- `POST /api/ai-assistant/remediation-guidance` - Get remediation guidance
- `POST /api/ai-assistant/service-risk-summary` - Summarize service risk
- `POST /api/ai-assistant/daily-security-brief` - Generate daily brief

---

## 🎯 8. Remediation Items and Lifecycle

### RemediationItem Domain
Represents an actionable unit of work for engineering teams.

### Fields
- `findingId` - Associated security finding
- `serviceName` - Service requiring remediation
- `teamName` - Team responsible for remediation
- `priority` - P0-P4 priority level
- `riskScore` - Calculated risk score (0-100)
- `remediationStatus` - NEW/OPEN/IN_PROGRESS/RESOLVED/ACCEPTED_RISK
- `recommendedAction` - Suggested remediation steps
- `firstDetectedAt` - When the finding was first detected
- `lastDetectedAt` - When the finding was last seen
- `latestScanAt` - Latest scan timestamp
- `resolvedAt` - When remediation was completed

### Status Transitions
Valid transitions:
- NEW → OPEN
- OPEN → IN_PROGRESS
- IN_PROGRESS → RESOLVED
- OPEN → ACCEPTED_RISK

**Important:** Findings are not automatically marked RESOLVED just because they're not detected in the latest scan. This requires explicit status update to prevent false positives.

### Remediation API Endpoints
- `GET /api/remediation` - List all remediation items
- `GET /api/remediation/{id}` - Get specific remediation item
- `GET /api/remediation/service/{serviceName}` - Items by service
- `GET /api/remediation/priority/{priority}` - Items by priority
- `GET /api/remediation/team/{teamName}` - Items by team
- `GET /api/remediation/action-center` - Action center summary
- `GET /api/remediation/top?limit=10` - Top priority items
- `PUT /api/remediation/{id}/status` - Update remediation status

---

## 📊 9. Security State Calculation

Each service has a calculated security state based on:

### Security States
- **HEALTHY:** Latest scan is fresh, no P0/P1 open findings
- **ATTENTION:** Has open P1 or significant P2 findings
- **CRITICAL:** Has open P0 findings
- **STALE:** Latest production scan older than threshold (default: 24 hours)
- **UNKNOWN:** No successful scan exists

### Freshness Thresholds
- **Production:** 24 hours (configurable via `SCAN_FRESHNESS_PRODUCTION_HOURS`)
- **Development:** 7 days (configurable via `SCAN_FRESHNESS_DEVELOPMENT_HOURS`)

### Calculation Logic
```
HAS SUCCESSFUL SCAN?
  ↓ NO
UNKNOWN
  ↓ YES
SCAN FRESH?
  ↓ NO
STALE
  ↓ YES
HAS OPEN P0?
  ↓ YES
CRITICAL
  ↓ NO
HAS OPEN P1 OR SIGNIFICANT P2?
  ↓ YES
ATTENTION
  ↓ NO
HEALTHY
```

---

## 📡 10. API Reference

### Dashboard Endpoints
- `GET /api/dashboard/summary` - Traditional dashboard metrics
- `GET /api/dashboard/action-center` - Action Center with prioritized decisions

### Remediation Endpoints
- `GET /api/remediation` - List all remediation items
- `GET /api/remediation/{id}` - Get specific remediation item
- `GET /api/remediation/action-center` - Action center summary
- `PUT /api/remediation/{id}/status` - Update remediation status

### Scan Execution Endpoints
- `GET /api/scans` - List all scan executions
- `GET /api/scans/{id}` - Get specific scan execution
- `GET /api/scans/service/{serviceName}` - Scans by service
- `GET /api/scans/recent?hours=24` - Recent scan activity
- `GET /api/scans/stale-summary` - Stale services summary

### AI Assistant Endpoints
- `GET /api/ai-assistant/configured` - Check AI configuration
- `POST /api/ai-assistant/explain-priority` - Explain finding priority
- `POST /api/ai-assistant/remediation-guidance` - Get remediation guidance
- `POST /api/ai-assistant/daily-security-brief` - Generate daily brief

### GitHub Actions Integration
- `POST /api/integrations/scans/github-actions` - Ingest scan from CI/CD
- `GET /api/integrations/scans/github-actions/health` - Health check

### Existing Endpoints (Preserved)
- `GET /api/findings` - List all findings
- `GET /api/findings/{id}` - Get finding details
- `POST /api/reports/upload` - Manual report upload
- `GET /api/services` - Service management
- `POST /api/dev/seed` - Seed sample data

---

## 🧪 11. Demo Services

The platform includes demo services to showcase GitHub Actions integration:

### Available Demo Services
- **payment-service** - Payment processing with vulnerable dependencies
- **order-service** - Order management with vulnerable dependencies  
- **auth-service** - Authentication service with vulnerable dependencies

### Setup Demo Services
See `demo-services/README.md` for detailed instructions on:
1. Creating private GitHub repositories
2. Configuring GitHub Secrets
3. Triggering security scans
4. Verifying report ingestion

### Expected Security Findings
Each demo service uses intentionally vulnerable npm packages to generate realistic security findings for testing the full platform capabilities.

---

## 🔧 12. Configuration

### Required Environment Variables
```bash
MONGODB_URI=mongodb+srv://admin:admin@cluster0.hs3mybp.mongodb.net/securityintel
SCAN_INGESTION_TOKEN=your-secret-token-here
```

### Optional Environment Variables
```bash
# Gemini AI Configuration
GEMINI_API_KEY=your-gemini-api-key-here
GEMINI_MODEL=gemini-1.5-flash

# Scan Freshness Configuration
SCAN_FRESHNESS_PRODUCTION_HOURS=24
SCAN_FRESHNESS_DEVELOPMENT_HOURS=168
```

### Application Configuration
See `backend/src/main/resources/application.yml` for additional configuration options.

---

## 🚀 13. Quick Start Guide

### Step 1: Start the Platform
```bash
# Start MongoDB
docker compose up -d mongodb

# Start Backend
cd backend
mvn spring-boot:run

# Start Frontend
cd frontend
npm start
```

### Step 2: Seed Sample Data
```bash
curl -X POST http://localhost:8080/api/dev/seed
```

### Step 3: Upload Sample Reports
```bash
# Upload Trivy report
curl -X POST http://localhost:8080/api/reports/upload \
  -F "file=@sample-data/sample-trivy-report.json" \
  -F "serviceName=payment-service" \
  -F "environment=PRODUCTION"

# Upload Snyk report
curl -X POST http://localhost:8080/api/reports/upload \
  -F "file=@sample-data/sample-snyk-report.json" \
  -F "serviceName=payment-service" \
  -F "environment=PRODUCTION"
```

### Step 4: Explore the Dashboard
- Open `http://localhost:4200`
- View the Security Action Center
- Check the Top Remediation Plan
- Review findings and priorities

### Step 5: Try AI Features (Optional)
- Configure `GEMINI_API_KEY` in `backend/.env`
- Restart the backend
- Use "Ask Gemini" features in finding details
- Generate a Daily Security Brief

### Step 6: Set Up GitHub Actions Integration
- Follow instructions in `demo-services/README.md`
- Create a private GitHub repository with a demo service
- Configure GitHub Secrets
- Trigger the security scan workflow
- Watch the platform automatically ingest and process the report

---

## 🔧 14. Troubleshooting

### Backend Issues
1. **MongoDB Connection Refused:**
   - Ensure MongoDB is running: `docker compose ps`
   - Check `MONGODB_URI` in backend/.env

2. **AI Features Not Working:**
   - Verify `GEMINI_API_KEY` is set in backend/.env
   - Check AI configuration: `curl http://localhost:8080/api/ai-assistant/configured`

3. **GitHub Actions Integration Failing:**
   - Verify `SCAN_INGESTION_TOKEN` matches between platform and GitHub Secrets
   - Check `SECURITY_INTEL_API_URL` is accessible from GitHub Actions
   - Use ngrok for local development: `ngrok http 8080`

### Frontend Issues
1. **CORS Errors:**
   - Backend controllers have `@CrossOrigin(origins = "*")` configured
   - Ensure backend is running on port 8080

2. **API Connection Issues:**
   - Check backend is accessible at `http://localhost:8080/api`
   - Verify no firewall blocking localhost connections

### Scan Processing Issues
1. **Report Parsing Fails:**
   - Ensure JSON follows Trivy or Snyk schema
   - Check file size doesn't exceed 10MB limit
   - Verify the report contains valid vulnerability data

2. **No Findings Detected:**
   - Check the scanner report actually contains findings
   - Verify service name matches existing services
   - Review parser logs for specific errors

---

## 🧪 15. Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Build
```bash
cd frontend
npm run build
```

### Integration Testing
For comprehensive testing, use the provided demo services and sample data to verify:
- Scan ingestion and processing
- Finding normalization and deduplication
- Scan comparison (NEW/UNCHANGED/NOT_DETECTED_IN_LATEST_SCAN)
- Remediation item creation and lifecycle
- Security state calculation
- AI guidance generation (if configured)
- GitHub Actions integration

---

## 📈 16. Product Evolution

### From Security Intelligence to Remediation Intelligence

**Previous Focus (Day-1 MVP):**
- "How many vulnerabilities do we have?"
- Dashboard metrics and statistics
- Finding aggregation and display

**Current Focus (Remediation Intelligence):**
- "What should we fix first?"
- "Why should we fix it first?"
- "Which team owns it?"
- "What action should they take?"
- "Is our security data current?"
- "What changed since the last scan?"

### Key Enhancements
1. **Action Over Metrics:** Dashboard prioritizes actionable decisions over statistics
2. **Remediation Lifecycle:** Track remediation items from detection to resolution
3. **Scan Comparison:** Understand what's new, unchanged, or resolved
4. **Security State:** Quick health assessment per service
5. **AI Guidance:** Contextual explanations and remediation steps
6. **CI/CD Integration:** Automated security scanning in development workflow
7. **Freshness Monitoring:** Ensure security data is current

---

## 🤝 17. Contributing

This platform is designed to be extended with:
- Additional scanner parsers (SonarQube, Fortify, etc.)
- More AI providers (OpenAI, Anthropic, etc.)
- Additional integrations (Jira, Slack, Teams)
- Custom prioritization rules
- Service-specific security policies

---

## 📄 18. License

This is a demonstration platform for security intelligence and remediation. Use responsibly and in accordance with your organization's security policies.

---

## 🆘 19. Support

For issues, questions, or contributions:
- Review the troubleshooting section
- Check the demo services setup guide
- Examine the API reference
- Review the configuration options

**Security Notice:** This platform handles security vulnerability data. Ensure proper access controls, audit logging, and security measures in production deployments.