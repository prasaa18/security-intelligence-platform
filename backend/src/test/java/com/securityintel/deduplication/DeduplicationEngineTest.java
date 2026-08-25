package com.securityintel.deduplication;

import com.securityintel.model.Environment;
import com.securityintel.model.ScanType;
import com.securityintel.model.SecurityFinding;
import com.securityintel.model.Severity;
import com.securityintel.model.Status;
import com.securityintel.model.Tool;
import com.securityintel.repository.SecurityFindingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeduplicationEngineTest {

    @Mock
    private SecurityFindingRepository securityFindingRepository;

    private DeduplicationEngine deduplicationEngine;

    @BeforeEach
    void setUp() {
        deduplicationEngine = new DeduplicationEngine(securityFindingRepository);
    }

    private SecurityFinding createFinding(String cve, String serviceName, String pkg, String version, Tool tool, Severity severity) {
        SecurityFinding finding = new SecurityFinding();
        finding.setReportId("report-1");
        finding.setCve(cve);
        finding.setServiceName(serviceName);
        finding.setPackageName(pkg);
        finding.setInstalledVersion(version);
        finding.setTool(tool);
        finding.setSeverity(severity);
        finding.setStatus(Status.OPEN);
        finding.setFingerprint("CVE:" + cve + "|SVC:" + serviceName + "|PKG:" + pkg + "|VER:" + version);
        finding.setSourceFindings(new ArrayList<>(List.of(tool.name() + ":report-1")));
        return finding;
    }

    @Test
    @DisplayName("Should detect new unique findings when no existing duplicate exists")
    void shouldDetectNewUniqueFindings() {
        SecurityFinding finding = createFinding("CVE-2024-1234", "payment-service", "openssl", "3.0.1", Tool.TRIVY, Severity.HIGH);

        when(securityFindingRepository.findByFingerprint(finding.getFingerprint())).thenReturn(Optional.empty());
        when(securityFindingRepository.findPotentialDuplicatesByCve("CVE-2024-1234", "payment-service", "openssl", "3.0.1"))
            .thenReturn(List.of());

        DeduplicationResult result = deduplicationEngine.processFindings(List.of(finding));

        assertEquals(1, result.getTotalRawFindings());
        assertEquals(1, result.getUniqueCount());
        assertEquals(0, result.getDuplicateFindings().size());
        assertEquals(1, result.getUniqueFindings().size());
    }

    @Test
    @DisplayName("Should correlate duplicate findings from Trivy and Snyk on same service and package")
    void shouldCorrelateDuplicateFindingsFromTrivyAndSnyk() {
        SecurityFinding existingTrivyFinding = createFinding("CVE-2024-1234", "payment-service", "openssl", "3.0.1", Tool.TRIVY, Severity.HIGH);
        existingTrivyFinding.setId("existing-1");

        SecurityFinding newSnykFinding = createFinding("CVE-2024-1234", "payment-service", "openssl", "3.0.1", Tool.SNYK, Severity.CRITICAL);
        newSnykFinding.setReportId("report-2");

        when(securityFindingRepository.findByFingerprint(newSnykFinding.getFingerprint())).thenReturn(Optional.of(existingTrivyFinding));

        DeduplicationResult result = deduplicationEngine.processFindings(List.of(newSnykFinding));

        assertEquals(1, result.getTotalRawFindings());
        assertEquals(1, result.getUniqueCount());
        assertEquals(1, result.getDuplicateFindings().size());
        
        SecurityFinding correlated = result.getUniqueFindings().get(0);
        assertEquals("existing-1", correlated.getId());
        assertEquals(Severity.CRITICAL, correlated.getSeverity()); // Upgraded to higher severity
        assertTrue(correlated.getSourceFindings().contains("TRIVY:report-1"));
        assertTrue(correlated.getSourceFindings().contains("SNYK:report-2"));
    }

    @Test
    @DisplayName("Should NOT correlate findings for different services")
    void shouldNotCorrelateFindingsForDifferentServices() {
        SecurityFinding paymentFinding = createFinding("CVE-2024-1234", "payment-service", "openssl", "3.0.1", Tool.TRIVY, Severity.HIGH);
        SecurityFinding orderFinding = createFinding("CVE-2024-1234", "order-service", "openssl", "3.0.1", Tool.TRIVY, Severity.HIGH);

        when(securityFindingRepository.findByFingerprint(paymentFinding.getFingerprint())).thenReturn(Optional.empty());
        when(securityFindingRepository.findPotentialDuplicatesByCve("CVE-2024-1234", "payment-service", "openssl", "3.0.1")).thenReturn(List.of());

        when(securityFindingRepository.findByFingerprint(orderFinding.getFingerprint())).thenReturn(Optional.empty());
        when(securityFindingRepository.findPotentialDuplicatesByCve("CVE-2024-1234", "order-service", "openssl", "3.0.1")).thenReturn(List.of());

        DeduplicationResult result = deduplicationEngine.processFindings(List.of(paymentFinding, orderFinding));

        assertEquals(2, result.getTotalRawFindings());
        assertEquals(2, result.getUniqueCount());
        assertEquals(0, result.getDuplicateFindings().size());
    }
}

