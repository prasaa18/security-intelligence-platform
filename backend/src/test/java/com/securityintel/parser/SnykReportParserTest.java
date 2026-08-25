package com.securityintel.parser;

import com.securityintel.model.Environment;
import com.securityintel.model.ScanType;
import com.securityintel.model.Severity;
import com.securityintel.model.Tool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SnykReportParserTest {

    private SnykReportParser parser;

    @BeforeEach
    void setUp() {
        parser = new SnykReportParser();
    }

    @Test
    @DisplayName("Should detect support for valid Snyk JSON")
    void shouldSupportSnykJson() {
        String snykJson = """
            {
              "vulnerabilities": [],
              "projectName": "payment-service",
              "packageManager": "npm"
            }
            """;
        assertTrue(parser.supports(snykJson));
    }

    @Test
    @DisplayName("Should not support non-Snyk JSON")
    void shouldNotSupportNonSnykJson() {
        String nonSnykJson = "{\"SchemaVersion\": 2, \"Results\": []}";
        assertFalse(parser.supports(nonSnykJson));
    }

    @Test
    @DisplayName("Should parse valid Snyk JSON report successfully")
    void shouldParseValidSnykReport() throws SecurityReportParseException {
        String snykJson = """
            {
              "vulnerabilities": [
                {
                  "id": "SNYK-JS-LODASH-567746",
                  "title": "Prototype Pollution",
                  "description": "Prototype pollution vulnerability in lodash",
                  "severity": "high",
                  "cvssScore": 7.5,
                  "packageName": "lodash",
                  "version": "4.17.15",
                  "fixedIn": ["4.17.21"],
                  "identifiers": {
                    "CVE": ["CVE-2020-8203"],
                    "CWE": ["CWE-1321"]
                  }
                }
              ],
              "projectName": "payment-service",
              "packageManager": "npm",
              "path": "/app/package.json"
            }
            """;

        ParsedSecurityReport report = parser.parse(snykJson, "payment-service", Environment.PRODUCTION);

        assertNotNull(report);
        assertEquals(Tool.SNYK, report.getTool());
        assertEquals("payment-service", report.getServiceName());
        assertEquals(Environment.PRODUCTION, report.getEnvironment());
        assertEquals(ScanType.SCA, report.getScanType());
        assertEquals(1, report.getFindings().size());

        ParsedFinding finding = report.getFindings().get(0);
        assertEquals("CVE-2020-8203", finding.getCve());
        assertEquals("lodash", finding.getPackageName());
        assertEquals("4.17.15", finding.getInstalledVersion());
        assertEquals("4.17.21", finding.getFixedVersion());
        assertEquals(Severity.HIGH, finding.getSeverity());
        assertEquals(7.5, finding.getCvssScore());
        assertEquals("CWE-1321", finding.getCwe());
        assertEquals("Prototype Pollution", finding.getTitle());
    }

    @Test
    @DisplayName("Should reject invalid JSON")
    void shouldRejectInvalidJson() {
        String invalidJson = "{ invalid snyk content }";
        assertFalse(parser.supports(invalidJson));
        assertThrows(SecurityReportParseException.class, () ->
            parser.parse(invalidJson, "service", Environment.DEVELOPMENT));
    }
}

