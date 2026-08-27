package com.securityintel.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securityintel.model.*;
import com.securityintel.repository.RemediationItemRepository;
import com.securityintel.repository.ScanExecutionRepository;
import com.securityintel.repository.SecurityFindingRepository;
import com.securityintel.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class GeminiAiService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;
    
    @Value("${gemini.api.model:gemini-3.6-flash}")
    private String geminiModel;
    
    @Value("${gemini.api.endpoint:https://generativelanguage.googleapis.com/v1beta/interactions}")
    private String geminiEndpoint;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SecurityFindingRepository securityFindingRepository;
    private final ServiceRepository serviceRepository;
    private final RemediationItemRepository remediationItemRepository;
    private final ScanExecutionRepository scanExecutionRepository;

    public GeminiAiService(RestTemplate restTemplate,
                          ObjectMapper objectMapper,
                          SecurityFindingRepository securityFindingRepository,
                          ServiceRepository serviceRepository,
                          RemediationItemRepository remediationItemRepository,
                          ScanExecutionRepository scanExecutionRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.securityFindingRepository = securityFindingRepository;
        this.serviceRepository = serviceRepository;
        this.remediationItemRepository = remediationItemRepository;
        this.scanExecutionRepository = scanExecutionRepository;
    }

    public boolean isConfigured() {
        return geminiApiKey != null && !geminiApiKey.isEmpty();
    }

    public String getMode() { return isConfigured() ? "AI_ENHANCED" : "SECURITY_ANALYST"; }
    public String getModel() { return geminiModel; }

    public String explainPriority(String findingId) {
        if (!isConfigured()) {
            SecurityFinding finding = securityFindingRepository.findById(findingId).orElseThrow(() -> new com.securityintel.exception.ResourceNotFoundException("Finding not found: " + findingId));
            return localPriorityExplanation(finding);
        }

        SecurityFinding finding = securityFindingRepository.findById(findingId)
            .orElseThrow(() -> new com.securityintel.exception.ResourceNotFoundException("Finding not found: " + findingId));

        String context = buildFindingContext(finding);
        String prompt = buildExplainPriorityPrompt(context);

        return callGeminiApi(prompt);
    }

    public String generateRemediationGuidance(String findingId) {
        if (!isConfigured()) {
            SecurityFinding finding = securityFindingRepository.findById(findingId).orElseThrow(() -> new com.securityintel.exception.ResourceNotFoundException("Finding not found: " + findingId));
            return localRemediationGuidance(finding);
        }

        SecurityFinding finding = securityFindingRepository.findById(findingId)
            .orElseThrow(() -> new com.securityintel.exception.ResourceNotFoundException("Finding not found: " + findingId));

        String context = buildFindingContext(finding);
        String prompt = buildRemediationGuidancePrompt(context);

        return callGeminiApi(prompt);
    }

    public String summarizeServiceRisk(String serviceId) {
        if (!isConfigured()) {
            Service service = serviceRepository.findById(serviceId).orElseThrow(() -> new com.securityintel.exception.ResourceNotFoundException("Service not found: " + serviceId));
            return localServiceSummary(service);
        }

        Service service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new com.securityintel.exception.ResourceNotFoundException("Service not found: " + serviceId));

        String context = buildServiceContext(service);
        String prompt = buildServiceRiskSummaryPrompt(context);

        return callGeminiApi(prompt);
    }

    public String generateDailySecurityBrief() {
        if (!isConfigured()) {
            return localDailyBrief();
        }

        String context = buildDailySecurityContext();
        String prompt = buildDailySecurityBriefPrompt(context);

        return callGeminiApi(prompt);
    }

    public String answerQuestion(String question, String serviceId, String findingId) {
        if (!isConfigured()) {
            return localQuestionAnswer(question, serviceId, findingId);
        }
        StringBuilder context = new StringBuilder();
        if (serviceId != null && !serviceId.isBlank()) {
            serviceRepository.findById(serviceId).ifPresent(service -> context.append(buildServiceContext(service)));
        }
        if (findingId != null && !findingId.isBlank()) {
            securityFindingRepository.findById(findingId).ifPresent(finding -> context.append("\n").append(buildFindingContext(finding)));
        }
        String prompt = "Answer the security team's question using only the supplied facts. " +
            "Lead with a direct answer, then give at most three actionable bullets. " +
            "Say when the facts are insufficient. Do not return JSON or metadata.\n\n" +
            "QUESTION: " + question + "\n\nFACTS:\n" + context;
        return callGeminiApi(prompt);
    }

    private String buildFindingContext(SecurityFinding finding) {
        StringBuilder context = new StringBuilder();
        
        context.append("FINDING FACTS:\n");
        context.append("CVE: ").append(finding.getCve() != null ? finding.getCve() : "N/A").append("\n");
        context.append("Title: ").append(finding.getTitle() != null ? finding.getTitle() : "N/A").append("\n");
        context.append("Scanner Severity: ").append(finding.getSeverity()).append("\n");
        context.append("CVSS Score: ").append(finding.getCvssScore() != null ? finding.getCvssScore() : "N/A").append("\n");
        context.append("Package: ").append(finding.getPackageName() != null ? finding.getPackageName() : "N/A").append("\n");
        context.append("Installed Version: ").append(finding.getInstalledVersion() != null ? finding.getInstalledVersion() : "N/A").append("\n");
        context.append("Fixed Version: ").append(finding.getFixedVersion() != null ? finding.getFixedVersion() : "N/A").append("\n");
        context.append("Service: ").append(finding.getServiceName()).append("\n");
        context.append("Environment: ").append(finding.getEnvironment()).append("\n");
        context.append("Calculated Risk Score: ").append(finding.getRiskScore()).append("\n");
        context.append("Priority: ").append(finding.getPriority()).append("\n");
        
        if (finding.getPriorityReasons() != null && !finding.getPriorityReasons().isEmpty()) {
            context.append("Priority Reasons:\n");
            for (String reason : finding.getPriorityReasons()) {
                context.append("- ").append(reason).append("\n");
            }
        }
        
        // Add service context
        Optional<com.securityintel.model.Service> service = serviceRepository.findByServiceName(finding.getServiceName());
        if (service.isPresent()) {
            com.securityintel.model.Service svc = service.get();
            context.append("\nSERVICE CONTEXT:\n");
            context.append("Business Criticality: ").append(svc.getBusinessCriticality()).append("\n");
            context.append("Internet Exposed: ").append(svc.isInternetExposed()).append("\n");
            context.append("Data Sensitivity: ").append(svc.getDataSensitivity()).append("\n");
            context.append("Owner Team: ").append(svc.getTeamName() != null ? svc.getTeamName() : "N/A").append("\n");
        }
        
        return context.toString();
    }

    private String buildServiceContext(com.securityintel.model.Service service) {
        StringBuilder context = new StringBuilder();
        
        context.append("SERVICE FACTS:\n");
        context.append("Service Name: ").append(service.getServiceName()).append("\n");
        context.append("Environment: ").append(service.getEnvironment()).append("\n");
        context.append("Business Criticality: ").append(service.getBusinessCriticality()).append("\n");
        context.append("Internet Exposed: ").append(service.isInternetExposed()).append("\n");
        context.append("Data Sensitivity: ").append(service.getDataSensitivity()).append("\n");
        context.append("Owner Team: ").append(service.getTeamName() != null ? service.getTeamName() : "N/A").append("\n");
        
        // Get latest scan info
        var latestScanOpt = scanExecutionRepository.findLatestSuccessfulScanByService(service.getServiceName());
        if (latestScanOpt.isPresent()) {
            var scan = latestScanOpt.get();
            context.append("\nLATEST SCAN:\n");
            context.append("Tool: ").append(scan.getTool()).append("\n");
            context.append("Scan Time: ").append(scan.getCompletedAt() != null ? scan.getCompletedAt() : scan.getCreatedAt()).append("\n");
            context.append("Total Findings: ").append(scan.getTotalUniqueFindings()).append("\n");
            context.append("Critical Count: ").append(scan.getCriticalCount()).append("\n");
            context.append("High Count: ").append(scan.getHighCount()).append("\n");
        }
        
        // Get open findings summary
        List<SecurityFinding> openFindings = securityFindingRepository.findActiveFindingsByService(service.getServiceName());
        context.append("\nOPEN FINDINGS SUMMARY:\n");
        context.append("Total Open: ").append(openFindings.size()).append("\n");
        context.append("P0: ").append(openFindings.stream().filter(f -> f.getPriority() == Priority.P0).count()).append("\n");
        context.append("P1: ").append(openFindings.stream().filter(f -> f.getPriority() == Priority.P1).count()).append("\n");
        context.append("P2: ").append(openFindings.stream().filter(f -> f.getPriority() == Priority.P2).count()).append("\n");
        
        return context.toString();
    }

    private String buildDailySecurityContext() {
        StringBuilder context = new StringBuilder();
        
        // Get overall statistics
        long openP0 = securityFindingRepository.countByPriorityAndStatus(Priority.P0, Status.OPEN);
        long openP1 = securityFindingRepository.countByPriorityAndStatus(Priority.P1, Status.OPEN);
        
        // Get all services to check for stale scans
        List<com.securityintel.model.Service> allServices = serviceRepository.findAll();
        long staleServices = 0;
        for (com.securityintel.model.Service service : allServices) {
            var latestScanOpt = scanExecutionRepository.findLatestSuccessfulScanByService(service.getServiceName());
            if (latestScanOpt.isEmpty()) {
                staleServices++;
            } else {
                LocalDateTime scanTime = latestScanOpt.get().getCompletedAt() != null ? 
                    latestScanOpt.get().getCompletedAt() : latestScanOpt.get().getCreatedAt();
                // Simple stale check (24 hours for production, 7 days for development)
                long hoursSinceScan = java.time.Duration.between(scanTime, LocalDateTime.now()).toHours();
                if ((service.getEnvironment() == Environment.PRODUCTION && hoursSinceScan > 24) ||
                    (service.getEnvironment() != Environment.PRODUCTION && hoursSinceScan > 168)) {
                    staleServices++;
                }
            }
        }
        
        // Get recent scan activity
        List<ScanExecution> recentScans = scanExecutionRepository.findByStatusOrderByCreatedAtDesc(Status.SUCCESS);
        
        context.append("SECURITY STATUS SUMMARY:\n");
        context.append("Open P0 Findings: ").append(openP0).append("\n");
        context.append("Open P1 Findings: ").append(openP1).append("\n");
        context.append("Services with Stale Scans: ").append(staleServices).append("\n");
        context.append("Total Services: ").append(allServices.size()).append("\n");
        context.append("Recent Successful Scans: ").append(Math.min(recentScans.size(), 10)).append("\n");
        
        return context.toString();
    }

    private String localPriorityExplanation(SecurityFinding finding) {
        return "Priority " + finding.getPriority() + " is based on " + finding.getSeverity() + " scanner severity, CVSS " + (finding.getCvssScore() == null ? "not provided" : finding.getCvssScore()) + ", and the business context of " + finding.getServiceName() + ".";
    }

    private String localRemediationGuidance(SecurityFinding finding) {
        String fix = finding.getFixedVersion() == null ? "the first patched version provided by the scanner" : finding.getFixedVersion();
        return "Update " + (finding.getPackageName() == null ? "the affected dependency" : finding.getPackageName()) + " from " + (finding.getInstalledVersion() == null ? "the installed version" : finding.getInstalledVersion()) + " to " + fix + ". Rebuild the service, rerun the same scanner, and confirm the finding is no longer detected.";
    }

    private String localServiceSummary(Service service) {
        List<SecurityFinding> findings = securityFindingRepository.findActiveFindingsByService(service.getServiceName());
        long p0 = findings.stream().filter(f -> f.getPriority() == Priority.P0).count();
        long p1 = findings.stream().filter(f -> f.getPriority() == Priority.P1).count();
        return service.getServiceName() + " has " + findings.size() + " active findings, including " + p0 + " P0 and " + p1 + " P1 items. Review the highest-priority fixes first; the service is " + service.getBusinessCriticality() + " criticality and runs in " + service.getEnvironment() + ".";
    }

    private String localDailyBrief() {
        long p0 = securityFindingRepository.countByPriorityAndStatus(Priority.P0, Status.OPEN);
        long p1 = securityFindingRepository.countByPriorityAndStatus(Priority.P1, Status.OPEN);
        return "Today's Security Brief\n\n- " + p0 + " open P0 findings require immediate action.\n- " + p1 + " open P1 findings should be planned this week.\n- Use the Security Hub service matrix to identify owners, stale scans, and the next remediation queue.\n- AI enrichment is unavailable; these facts come directly from scanner-derived platform data.";
    }

    private String localQuestionAnswer(String question, String serviceId, String findingId) {
        if (findingId != null && !findingId.isBlank()) return localRemediationGuidance(securityFindingRepository.findById(findingId).orElseThrow(() -> new com.securityintel.exception.ResourceNotFoundException("Finding not found: " + findingId)));
        if (serviceId != null && !serviceId.isBlank()) return localServiceSummary(serviceRepository.findById(serviceId).orElseThrow(() -> new com.securityintel.exception.ResourceNotFoundException("Service not found: " + serviceId)));
        return "I can answer questions about active priorities, scanner findings, remediation, service owners, and scan freshness. Select a service or finding for a grounded answer. Your question was: " + question;
    }

    private String buildExplainPriorityPrompt(String context) {
        return "Explain why this vulnerability received its priority level. Be concise and clear.\n\n" +
               context + "\n\n" +
               "Provide a brief explanation in 2-3 sentences. Focus on the key risk factors.";
    }

    private String buildRemediationGuidancePrompt(String context) {
        return "Provide brief remediation guidance for this vulnerability.\n\n" +
               context + "\n\n" +
               "Give 2-3 specific steps to fix this issue. Keep it actionable and concise.";
    }

    private String buildServiceRiskSummaryPrompt(String context) {
        return "Summarize the security risk profile of this service.\n\n" +
               context + "\n\n" +
               "Give a 2-3 sentence assessment focusing on the top risks and recommended actions.";
    }

    private String buildDailySecurityBriefPrompt(String context) {
        return "Generate a brief daily security summary.\n\n" +
               context + "\n\n" +
               "Provide 3-4 bullet points on what needs attention today and which services need focus.";
    }

    // Simple cache for AI responses to improve performance
    private final java.util.Map<String, String> responseCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    private String callGeminiApi(String prompt) {
        try {
            // Check cache first using simple hash of prompt
            String cacheKey = String.valueOf(prompt.hashCode());
            
            if (responseCache.containsKey(cacheKey)) {
                return responseCache.get(cacheKey);
            }
            
            String url = geminiEndpoint + "?key=" + geminiApiKey;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Api-Revision", "2026-05-20");
            
            // Simplified request for faster response
            Map<String, Object> requestBody = Map.of(
                "model", geminiModel,
                "input", prompt,
                "store", false
            );
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                
                // Try to extract content from the new Gemini response format with steps
                if (root.has("steps") && root.path("steps").isArray()) {
                    for (JsonNode step : root.path("steps")) {
                        if (step.has("type") && step.path("type").asText().equals("model_output")) {
                            if (step.has("content") && step.path("content").isArray()) {
                                JsonNode contentArray = step.path("content");
                                if (contentArray.size() > 0 && contentArray.get(0).has("text")) {
                                    String result = contentArray.get(0).path("text").asText();
                                    responseCache.put(cacheKey, result);
                                    return result;
                                }
                            }
                        }
                    }
                }
                
                // Fallback to legacy response format parsing
                JsonNode reply = root.path("reply");
                if (reply.has("content")) {
                    String result = reply.path("content").asText();
                    responseCache.put(cacheKey, result);
                    return result;
                } else if (reply.has("text")) {
                    String result = reply.path("text").asText();
                    responseCache.put(cacheKey, result);
                    return result;
                } else if (root.has("reply") && root.path("reply").isArray() && root.path("reply").size() > 0) {
                    JsonNode firstReply = root.path("reply").get(0);
                    if (firstReply.has("content")) {
                        String result = firstReply.path("content").asText();
                        responseCache.put(cacheKey, result);
                        return result;
                    } else if (firstReply.has("text")) {
                        String result = firstReply.path("text").asText();
                        responseCache.put(cacheKey, result);
                        return result;
                    }
                } else {
                    // Try to get any text content from the response
                    String responseText = response.getBody();
                    if (responseText != null && !responseText.trim().isEmpty()) {
                        responseCache.put(cacheKey, responseText);
                        return responseText;
                    }
                }
            }
            
            return "Failed to parse Gemini response. Status: " + response.getStatusCode();
            
        } catch (Exception e) {
            return "Error calling Gemini API: " + e.getMessage();
        }
    }
}