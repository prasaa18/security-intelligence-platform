package com.securityintel.prioritization;

import com.securityintel.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityPrioritizationEngineTest {

    private SecurityPrioritizationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new SecurityPrioritizationEngine();
    }

    @Test
    @DisplayName("Should correctly calculate deterministic risk score and priority P0")
    void shouldCalculateP0Priority() {
        SecurityFinding finding = new SecurityFinding();
        finding.setSeverity(Severity.CRITICAL); // 70
        finding.setCvssScore(9.8); // 10
        finding.setEnvironment(Environment.PRODUCTION); // +15

        Service service = new Service();
        service.setBusinessCriticality(BusinessCriticality.CRITICAL); // +20
        service.setInternetExposed(true); // +20
        service.setDataSensitivity(DataSensitivity.SENSITIVE); // +10

        PriorityResult result = engine.calculatePriority(finding, service);

        // 70 + 10 + 15 + 20 + 20 + 10 = 145 capped at 100
        assertEquals(100, result.getRiskScore());
        assertEquals(Priority.P0, result.getPriority());
        assertTrue(result.getReasons().contains("Critical severity vulnerability"));
        assertTrue(result.getReasons().contains("Production environment"));
        assertTrue(result.getReasons().contains("Internet-facing service"));
        assertTrue(result.getReasons().contains("Critical business service"));
        assertTrue(result.getReasons().contains("Handles sensitive data"));
    }

    @Test
    @DisplayName("Should demonstrate that context matters: Finding B (HIGH severity in PROD) > Finding A (CRITICAL in DEV)")
    void shouldDemonstrateContextMatters() {
        // Finding A: CRITICAL severity in DEV without exposure
        SecurityFinding findingA = new SecurityFinding();
        findingA.setSeverity(Severity.CRITICAL); // 70
        findingA.setCvssScore(9.5); // 10
        findingA.setEnvironment(Environment.DEVELOPMENT); // +0

        Service serviceA = new Service();
        serviceA.setEnvironment(Environment.DEVELOPMENT);
        serviceA.setBusinessCriticality(BusinessCriticality.LOW); // +0
        serviceA.setInternetExposed(false); // +0
        serviceA.setDataSensitivity(DataSensitivity.INTERNAL); // +0

        PriorityResult resultA = engine.calculatePriority(findingA, serviceA);
        // Result A: 70 + 10 = 80 -> P1

        // Finding B: HIGH severity in PROD, internet-exposed, critical business, sensitive data
        SecurityFinding findingB = new SecurityFinding();
        findingB.setSeverity(Severity.HIGH); // 55
        findingB.setCvssScore(8.5); // 9
        findingB.setEnvironment(Environment.PRODUCTION); // +15

        Service serviceB = new Service();
        serviceB.setEnvironment(Environment.PRODUCTION);
        serviceB.setBusinessCriticality(BusinessCriticality.CRITICAL); // +20
        serviceB.setInternetExposed(true); // +20
        serviceB.setDataSensitivity(DataSensitivity.SENSITIVE); // +10

        PriorityResult resultB = engine.calculatePriority(findingB, serviceB);
        // Result B: 55 + 9 + 15 + 20 + 20 + 10 = 100 (capped) -> P0 (or 96 if rounding)

        assertTrue(resultB.getRiskScore() > resultA.getRiskScore(), 
            "Finding B with PROD context must have higher risk score than Finding A in DEV");
        assertEquals(Priority.P0, resultB.getPriority());
        assertEquals(Priority.P1, resultA.getPriority());
    }

    @Test
    @DisplayName("Should verify priority score ranges mapping")
    void shouldMapPriorityRangesCorrectly() {
        // Test LOW finding in DEV with LOW service context
        SecurityFinding lowFinding = new SecurityFinding();
        lowFinding.setSeverity(Severity.LOW); // 15
        lowFinding.setCvssScore(2.0); // 2
        lowFinding.setEnvironment(Environment.DEVELOPMENT);

        Service lowService = new Service();
        lowService.setBusinessCriticality(BusinessCriticality.LOW);
        lowService.setInternetExposed(false);
        lowService.setDataSensitivity(DataSensitivity.PUBLIC);

        PriorityResult lowResult = engine.calculatePriority(lowFinding, lowService);
        // Risk score = 17 -> P4 (0-29)
        assertEquals(17, lowResult.getRiskScore());
        assertEquals(Priority.P4, lowResult.getPriority());
    }
}

