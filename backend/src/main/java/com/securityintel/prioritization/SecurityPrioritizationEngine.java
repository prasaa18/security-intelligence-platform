package com.securityintel.prioritization;

import com.securityintel.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SecurityPrioritizationEngine {

    /**
     * Calculate remediation priority for a security finding based on context
     * @param finding The security finding to prioritize
     * @param service The service context (can be null)
     * @return PriorityResult with risk score, priority, and reasons
     */
    public PriorityResult calculatePriority(SecurityFinding finding, Service service) {
        List<String> reasons = new ArrayList<>();
        int riskScore = 0;

        if (finding == null) {
            return new PriorityResult(0, Priority.P4, List.of("No finding data"));
        }

        // Base severity score
        int severityScore = calculateSeverityScore(finding.getSeverity());
        riskScore += severityScore;
        if (severityScore > 0) {
            reasons.add(getSeverityReason(finding.getSeverity()));
        }

        // CVSS contribution
        if (finding.getCvssScore() != null && finding.getCvssScore() > 0) {
            int cvssContribution = (int) Math.round((finding.getCvssScore() / 10.0) * 10);
            riskScore += cvssContribution;
            reasons.add(String.format("CVSS score %.1f", finding.getCvssScore()));
        }

        // Service context scoring (if service information is available)
        if (service != null) {
            // Environment context
            if (finding.getEnvironment() == Environment.PRODUCTION) {
                riskScore += 15;
                reasons.add("Production environment");
            } else if (finding.getEnvironment() == Environment.STAGING) {
                riskScore += 5;
                reasons.add("Staging environment");
            }

            // Internet exposure
            if (service.isInternetExposed()) {
                riskScore += 20;
                reasons.add("Internet-facing service");
            }

            // Business criticality
            if (service.getBusinessCriticality() != null) {
                if (service.getBusinessCriticality() == BusinessCriticality.CRITICAL) {
                    riskScore += 20;
                    reasons.add("Critical business service");
                } else if (service.getBusinessCriticality() == BusinessCriticality.HIGH) {
                    riskScore += 10;
                    reasons.add("High business criticality");
                } else if (service.getBusinessCriticality() == BusinessCriticality.MEDIUM) {
                    riskScore += 5;
                    reasons.add("Medium business criticality");
                }
            }

            // Data sensitivity
            if (service.getDataSensitivity() != null) {
                if (service.getDataSensitivity() == DataSensitivity.HIGHLY_SENSITIVE) {
                    riskScore += 15;
                    reasons.add("Handles highly sensitive data");
                } else if (service.getDataSensitivity() == DataSensitivity.SENSITIVE) {
                    riskScore += 10;
                    reasons.add("Handles sensitive data");
                } else if (service.getDataSensitivity() == DataSensitivity.CONFIDENTIAL) {
                    riskScore += 5;
                    reasons.add("Handles confidential data");
                }
            }
        } else {
            // If no service context, check finding environment
            if (finding.getEnvironment() == Environment.PRODUCTION) {
                riskScore += 15;
                reasons.add("Production environment");
            }
        }

        // Cap the score at 100
        riskScore = Math.min(riskScore, 100);

        // Determine priority based on risk score
        Priority priority = determinePriority(riskScore);

        return new PriorityResult(riskScore, priority, reasons);
    }

    private int calculateSeverityScore(Severity severity) {
        if (severity == null) {
            return 5; // UNKNOWN
        }

        if (severity == Severity.CRITICAL) return 70;
        if (severity == Severity.HIGH) return 55;
        if (severity == Severity.MEDIUM) return 35;
        if (severity == Severity.LOW) return 15;
        return 5;
    }

    private String getSeverityReason(Severity severity) {
        if (severity == null) {
            return "Unknown severity";
        }

        if (severity == Severity.CRITICAL) return "Critical severity vulnerability";
        if (severity == Severity.HIGH) return "High severity vulnerability";
        if (severity == Severity.MEDIUM) return "Medium severity vulnerability";
        if (severity == Severity.LOW) return "Low severity vulnerability";
        return "Unknown severity vulnerability";
    }

    private Priority determinePriority(int riskScore) {
        if (riskScore >= 90) {
            return Priority.P0;
        } else if (riskScore >= 75) {
            return Priority.P1;
        } else if (riskScore >= 55) {
            return Priority.P2;
        } else if (riskScore >= 30) {
            return Priority.P3;
        } else {
            return Priority.P4;
        }
    }

    /**
     * Batch calculate priorities for multiple findings
     * @param findings List of findings with their service contexts
     * @return List of PriorityResults in same order as input
     */
    public List<PriorityResult> calculatePriorities(List<FindingWithService> findings) {
        List<PriorityResult> results = new ArrayList<>();
        if (findings == null) return results;

        for (FindingWithService findingWithService : findings) {
            PriorityResult result = calculatePriority(
                findingWithService.getFinding(), 
                findingWithService.getService()
            );
            results.add(result);
        }

        return results;
    }

    /**
     * Helper class to hold finding with its service context
     */
    public static class FindingWithService {
        private final SecurityFinding finding;
        private final Service service;

        public FindingWithService(SecurityFinding finding, Service service) {
            this.finding = finding;
            this.service = service;
        }

        public SecurityFinding getFinding() {
            return finding;
        }

        public Service getService() {
            return service;
        }
    }

    /**
     * Demonstrates the context importance as specified in requirements
     */
    public DemonstrationResult demonstrateContextImportance() {
        SecurityFinding findingA = new SecurityFinding();
        findingA.setSeverity(Severity.CRITICAL);
        findingA.setEnvironment(Environment.DEVELOPMENT);
        findingA.setCvssScore(9.5);

        Service serviceA = new Service();
        serviceA.setEnvironment(Environment.DEVELOPMENT);
        serviceA.setBusinessCriticality(BusinessCriticality.LOW);
        serviceA.setInternetExposed(false);
        serviceA.setDataSensitivity(DataSensitivity.INTERNAL);

        SecurityFinding findingB = new SecurityFinding();
        findingB.setSeverity(Severity.HIGH);
        findingB.setEnvironment(Environment.PRODUCTION);
        findingB.setCvssScore(8.5);

        Service serviceB = new Service();
        serviceB.setEnvironment(Environment.PRODUCTION);
        serviceB.setBusinessCriticality(BusinessCriticality.CRITICAL);
        serviceB.setInternetExposed(true);
        serviceB.setDataSensitivity(DataSensitivity.SENSITIVE);

        PriorityResult resultA = calculatePriority(findingA, serviceA);
        PriorityResult resultB = calculatePriority(findingB, serviceB);

        return new DemonstrationResult(
            findingA, resultA, serviceA,
            findingB, resultB, serviceB
        );
    }

    /**
     * Result class for demonstration
     */
    public static class DemonstrationResult {
        private final SecurityFinding findingA;
        private final PriorityResult resultA;
        private final Service serviceA;
        private final SecurityFinding findingB;
        private final PriorityResult resultB;
        private final Service serviceB;

        public DemonstrationResult(SecurityFinding findingA, PriorityResult resultA, Service serviceA,
                                 SecurityFinding findingB, PriorityResult resultB, Service serviceB) {
            this.findingA = findingA;
            this.resultA = resultA;
            this.serviceA = serviceA;
            this.findingB = findingB;
            this.resultB = resultB;
            this.serviceB = serviceB;
        }

        public boolean contextMatters() {
            return resultB.getRiskScore() > resultA.getRiskScore();
        }

        public SecurityFinding getFindingA() { return findingA; }
        public PriorityResult getResultA() { return resultA; }
        public Service getServiceA() { return serviceA; }
        public SecurityFinding getFindingB() { return findingB; }
        public PriorityResult getResultB() { return resultB; }
        public Service getServiceB() { return serviceB; }
    }
}