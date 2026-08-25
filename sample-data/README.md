# Sample Security Scan Reports

This directory contains example security scan reports for testing and demonstration purposes.

## Files

### `trivy-report-example.json`
Example Trivy container scanning report containing:
- **Service**: payment-service:v1.2.0
- **Vulnerabilities**: 4 findings (3 OS packages, 1 Java dependency)
- **Severities**: 1 Critical, 2 High, 1 Medium
- **Package types**: Ubuntu OS packages and Java JARs
- **CVEs**: CVE-2024-1234 (OpenSSL), CVE-2023-5678 (curl), CVE-2023-9999 (glibc), CVE-2023-4444 (Jackson)

**Key features demonstrated:**
- Container image scanning
- Multi-layer vulnerability detection
- OS package vulnerabilities
- Java application vulnerabilities
- CVSS scoring from multiple sources
- Fix version recommendations

### `snyk-report-example.json` 
Example Snyk dependency scanning report containing:
- **Service**: payment-service
- **Vulnerabilities**: 4 findings (3 JavaScript, 1 Java)
- **Severities**: 2 High, 2 Medium
- **Package types**: npm and Maven dependencies
- **CVEs**: CVE-2021-23337 (lodash), CVE-2022-22950 (Spring), CVE-2023-45857 (axios), CVE-2022-29078 (express)

**Key features demonstrated:**
- Dependency scanning
- Prototype pollution vulnerabilities
- Expression language injection
- Regular expression denial of service (ReDoS)
- Open redirect vulnerabilities
- Upgrade path recommendations
- Exploit maturity indicators

## Usage

These sample files can be used to:

1. **Test the Security Intelligence Platform**:
   ```bash
   # Upload via API
   curl -X POST http://localhost:8080/api/v1/reports/process \
     -H "Content-Type: application/json" \
     -d @trivy-report-example.json \
     -G -d serviceName=payment-service -d environment=PRODUCTION
   ```

2. **Seed Development Data**:
   - Use the `/api/v1/dev/seed-sample-data` endpoint in the backend
   - Upload through the frontend reports page

3. **Integration Testing**:
   - Verify parser functionality
   - Test deduplication logic
   - Validate prioritization engine
   - Check dashboard calculations

## Real-world Scenarios Covered

### Critical Production Issues
- **CVE-2024-1234**: Critical OpenSSL vulnerability affecting internet-exposed services
- **CVE-2022-22950**: Spring Framework RCE vulnerability

### Common Development Issues  
- **CVE-2021-23337**: Lodash prototype pollution (affects many Node.js apps)
- **CVE-2023-45857**: Axios ReDoS vulnerability (performance impact)
- **CVE-2022-29078**: Express open redirect (security misconfiguration)

### Mixed Environments
- Containerized applications with OS-level vulnerabilities
- Multi-language applications (Java + JavaScript)
- Dependencies with different fix availability
- Various CVSS score sources and methodologies

## Security Note

These are synthetic examples for testing purposes only. Real vulnerability data should be handled according to your organization's security policies.