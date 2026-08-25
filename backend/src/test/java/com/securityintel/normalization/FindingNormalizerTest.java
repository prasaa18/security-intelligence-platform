package com.securityintel.normalization;

import com.securityintel.model.Environment;
import com.securityintel.model.ScanType;
import com.securityintel.model.SecurityFinding;
import com.securityintel.model.Severity;
import com.securityintel.model.Tool;
import com.securityintel.parser.ParsedFinding;
import com.securityintel.parser.ParsedSecurityReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FindingNormalizerTest {

    private FindingNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new FindingNormalizer();
    }

    @Test
    @DisplayName("Should normalize parsed findings correctly with primary fingerprint")
    void shouldNormalizeParsedFindingsWithPrimaryFingerprint() {
        ParsedSecurityReport report = new ParsedSecurityReport();
        report.setTool(Tool.TRIVY);
        report.setScanType(ScanType.CONTAINER);
        report.setServiceName("payment-service");
        report.setEnvironment(Environment.PRODUCTION);
        report.setRepository("repo-payment");

        ParsedFinding parsedFinding = new ParsedFinding();
        parsedFinding.setCve("CVE-2024-1234");
        parsedFinding.setCwe("CWE-120");
        parsedFinding.setTitle("Buffer Overflow");
        parsedFinding.setDescription("Critical memory flaw");
        parsedFinding.setPackageName("OpenSSL");
        parsedFinding.setInstalledVersion("v3.0.1");
        parsedFinding.setFixedVersion("3.0.2");
        parsedFinding.setSeverity(Severity.HIGH);
        parsedFinding.setCvssScore(8.5);

        report.setFindings(List.of(parsedFinding));

        List<SecurityFinding> normalized = normalizer.normalize(report, "report-123");

        assertEquals(1, normalized.size());
        SecurityFinding finding = normalized.get(0);

        assertEquals("report-123", finding.getReportId());
        assertEquals(Tool.TRIVY, finding.getTool());
        assertEquals(ScanType.CONTAINER, finding.getScanType());
        assertEquals("payment-service", finding.getServiceName());
        assertEquals(Environment.PRODUCTION, finding.getEnvironment());
        assertEquals(Severity.HIGH, finding.getSeverity());
        assertEquals(8.5, finding.getCvssScore());
        assertEquals("CVE-2024-1234", finding.getCve());
        assertEquals("CWE-120", finding.getCwe());
        assertEquals("Buffer Overflow", finding.getTitle());
        assertEquals("openssl", finding.getPackageName()); // cleaned to lowercase
        assertEquals("3.0.1", finding.getInstalledVersion()); // cleaned leading 'v'
        assertEquals("3.0.2", finding.getFixedVersion());
        assertNotNull(finding.getFingerprint());
        assertTrue(finding.getFingerprint().startsWith("CVE:CVE-2024-1234|SVC:payment-service|PKG:openssl|VER:3.0.1"));
    }

    @Test
    @DisplayName("Should generate fallback fingerprint when CVE is missing")
    void shouldGenerateFallbackFingerprintWhenCveMissing() {
        ParsedSecurityReport report = new ParsedSecurityReport();
        report.setTool(Tool.SNYK);
        report.setScanType(ScanType.SAST);
        report.setServiceName("order-service");
        report.setEnvironment(Environment.DEVELOPMENT);

        ParsedFinding parsedFinding = new ParsedFinding();
        parsedFinding.setTitle("SQL Injection");
        parsedFinding.setPackageName("mysql-connector");
        parsedFinding.setFilePath("src/main/db/Query.java");
        parsedFinding.setLineNumber(42);
        parsedFinding.setSeverity(Severity.HIGH);

        report.setFindings(List.of(parsedFinding));

        List<SecurityFinding> normalized = normalizer.normalize(report, "report-456");

        assertEquals(1, normalized.size());
        SecurityFinding finding = normalized.get(0);
        assertNull(finding.getCve());
        assertTrue(finding.getFingerprint().startsWith("TOOL:SNYK|SVC:order-service|TITLE:SQL Injection|PKG:mysql-connector|PATH:src/main/db/Query.java|LINE:42"));
    }
}

