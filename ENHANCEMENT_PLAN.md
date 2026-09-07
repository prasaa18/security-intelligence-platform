# Security Intelligence Platform Enhancement Plan

## Objective
Turn the app from a functional security dashboard into a premium application security command center with strong operational UX, clear risk prioritization, and visible product depth.

## Phase 1: Security Command Center
- Upgrade the dashboard into a true SOC-style overview
- Add executive readiness, risk split, and control coverage widgets
- Surface the most important open issues and service-level risk in the first screen

## Phase 2: Real-time AppSec Experience
- Add live security pulse and scan-activity streams
- Show detection, coverage, and remediation state continuously
- Make status and urgency visible without drilling into multiple pages

## Phase 3: Risk Triage and Prioritization
- Rank services by risk and remediation urgency
- Highlight critical findings, stale coverage, and app exposure patterns
- Improve actionability for engineering and security leadership

## Phase 4: Compliance and Evidence Readiness
- Keep compliance views and diff comparison tightly connected to the current posture
- Provide clear readiness and control-state reporting across services
- Ensure the app tells the story of risk, action, and evidence

## Phase 5: Quality Lock
- Validate build health and route integrity
- Confirm the product remains smooth and reliable under real data
- Keep enhancement work focused on user value without breaking workflows

## Current delivery focus
1. Executive dashboard operations layer
2. Live appsec pulse feed
3. Risk matrix and service coverage gaps
4. Better cross-navigation and visible decision support

## Commercial Readiness Execution Plan

### Phase 0 - Production safety baseline (started)
- Make MongoDB and scan-ingestion secrets mandatory outside development.
- Disable seed and purge endpoints unless the `dev` Spring profile is active.
- Remove public-facing default credentials from deployment configuration.
- Add a production deployment checklist covering secrets, backups, TLS, health checks, and log redaction.

### Phase 1 - Identity and workspace security
- Add user accounts with secure password hashing or OIDC login. **Started:** secure profile provides stateless HTTP Basic users from environment variables.
- Add organization/workspace membership and tenant-scoped data access.
- Add roles: platform admin, security lead, engineer, and read-only auditor. **Started:** admin/security-lead and viewer roles are available in the secure profile.
- Protect every API route and issue short-lived session tokens. **Started:** secure profile protects APIs; token/session migration remains.
- Add rate limiting and security headers at the edge.

### Phase 2 - Auditability and operational trust
- Record who uploaded scans, changed remediation state, exported data, or changed service ownership.
- Add immutable audit event search and retention settings.
- Add backup/restore documentation and a tested recovery procedure.
- Add health, readiness, database, and ingestion telemetry for operators.

### Phase 3 - Customer workflow integrations
- Create Jira and GitHub issues from remediation items with idempotency.
- Add Slack and generic webhook notifications for P0 findings, stale services, and scan failures.
- Add GitLab CI and Jenkins ingestion adapters using scoped project tokens.
- Add signed webhook verification and replay protection.

### Phase 4 - Product packaging
- Add first-run workspace onboarding and a sample-data sandbox isolated from customer data.
- Add plan limits, usage metering, and a customer-facing settings page.
- Add documented API contracts, integration examples, and troubleshooting runbooks.
- Add pricing/packaging around small-team value: remediation intelligence, ownership, and evidence readiness.

### Phase 5 - Launch quality gate
- Backend unit, integration, security, and authorization tests pass.
- Frontend production build and critical browser journeys pass.
- No default secrets, wildcard production CORS, exposed destructive endpoints, or cross-tenant reads remain.
- Complete a pilot with at least one external small engineering team before public launch.

### Current status
- Core AppSec workflow: implemented.
- Scan comparison recovery: implemented and browser-verified.
- Production safety baseline: in progress.
- Single-instance Docker deployment: prepared; only the frontend port is public and integrations use the same public `/api` URL.
- Secure API profile: started with stateless HTTP Basic roles.
- Multi-tenant SaaS readiness: not started.
