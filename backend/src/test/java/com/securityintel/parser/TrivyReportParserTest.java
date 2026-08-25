package com.securityintel.parser;

import com.securityintel.model.Environment;
import com.securityintel.model.ScanType;
import com.securityintel.model.Severity;
import com.securityintel.model.Tool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrivyReportParserTest {

    private TrivyReportParser parser;

    @BeforeEach
    void setUp() {
        parser = new TrivyReportParser();
    }

    @Test
    @DisplayName("Should detect support for valid Trivy JSON")
    void shouldSupportTrivyJson() {
        String trivyJson = """
            {
              "SchemaVersion": 2,
              "ArtifactName": "payment-service:latest",
              "ArtifactType": "container_image",
              "Results": []
            }
            """;
        assertTrue(parser.supports(trivyJson));
    }

    @Test
    @DisplayName("Should not support non-Trivy JSON")
    void shouldNotSupportNonTrivyJson() {
        String nonTrivyJson = "{\"snyk\": true, \"vulnerabilities\": []}";
        assertFalse(parser.supports(nonTrivyJson));
    }

    @Test
    @DisplayName("Should parse valid Trivy JSON report successfully")
    void shouldParseValidTrivyReport() throws SecurityReportParseException {
        String trivyJson = """
            {
              "SchemaVersion": 2,
              "ArtifactName": "payment-service",
              "ArtifactType": "container_image",
              "Results": [
                {
                  "Target": "payment-service (ubuntu 22.04)",
                  "Class": "os-pkgs",
                  "Type": "ubuntu",
                  "Vulnerabilities": [
                    {
                      "VulnerabilityID": "CVE-2024-1234",
                      "PkgName": "openssl",
                      "InstalledVersion": "3.0.2",
                      "FixedVersion": "3.0.3",
                      "Severity": "CRITICAL",
                      "Title": "Buffer overflow in OpenSSL",
                      "Description": "Memory corruption flaw",
                      "PrimaryURL": "https://avd.aquasec.com/nvd/cve-2024-1234",
                      "CweIDs": ["CWE-120"],
                      "CVSS": {
                        "nvd": {
                          "V3Score": 9.8
                        }
                      }
                    }
                  ]
                }
              ]
            }
            """;

        ParsedSecurityReport report = parser.parse(trivyJson, "payment-service", Environment.PRODUCTION);

        assertNotNull(report);
        assertEquals(Tool.TRIVY, report.getTool());
        assertEquals("payment-service", report.getServiceName());
        assertEquals(Environment.PRODUCTION, report.getEnvironment());
        assertEquals(ScanType.CONTAINER, report.getScanType());
        assertEquals(1, report.getFindings().size());

        ParsedFinding finding = report.getFindings().get(0);
        assertEquals("CVE-2024-1234", finding.getCve());
        assertEquals("openssl", finding.getPackageName());
        assertEquals("3.0.2", finding.getInstalledVersion());
        assertEquals("3.0.3", finding.getFixedVersion());
        assertEquals(Severity.CRITICAL, finding.getSeverity());
        assertEquals(9.8, finding.getCvssScore());
        assertEquals("CWE-120", finding.getCwe());
        assertEquals("Buffer overflow in OpenSSL", finding.getTitle());
    }

    @Test
    @DisplayName("Should reject invalid JSON")
    void shouldRejectInvalidJson() {
        String invalidJson = "{ invalid json content }";
        assertFalse(parser.supports(invalidJson));
        assertThrows(SecurityReportParseException.class, () -> 
            parser.parse(invalidJson, "service", Environment.DEVELOPMENT));
    }
}

