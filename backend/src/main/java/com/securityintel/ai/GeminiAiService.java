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
    
    @Value("${gemini.model:gemini-1.5-flash}")
    private String geminiModel;

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

    public String explainPriority(String findingId) {
        if (!isConfigured()) {
            return "Gemini AI is not configured. Please set GEMINI_API_KEY environment variable.";
        }

        SecurityFinding finding = securityFindingRepository.findById(findingId)
            .orElseThrow(() -> new com.securityintel.exception.ResourceNotFoundException("Finding not found: " + findingId));

        String context = buildFindingContext(finding);
        String prompt = buildExplainPriorityPrompt(context);

        return callGeminiApi(prompt);
    }

    public String generateRemediationGuidance(String findingId) {
        if (!isConfigured()) {
            return "Gemini AI is not configured. Please set GEMINI_API_KEY environment variable.";
        }

        SecurityFinding finding = securityFindingRepository.findById(findingId)
            .orElseThrow(() -> new com.securityintel.exception.ResourceNotFoundException("Finding not found: " + findingId));

        String context = buildFindingContext(finding);
        String prompt = buildRemediationGuidancePrompt(context);

        return callGeminiApi(prompt);
    }

    public String summarizeServiceRisk(String serviceId) {
        if (!isConfigured()) {
            return "Gemini AI is not configured. Please set GEMINI_API_KEY environment variable.";
        }

        Service service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new com.securityintel.exception.ResourceNotFoundException("Service not found: " + serviceId));

        String context = buildServiceContext(service);
        String prompt = buildServiceRiskSummaryPrompt(context);

        return callGeminiApi(prompt);
    }

    public String generateDailySecurityBrief() {
        if (!isConfigured()) {
            return "Gemini AI is not configured. Please set GEMINI_API_KEY environment variable.";
        }

        String context = buildDailySecurityContext();
        String prompt = buildDailySecurityBriefPrompt(context);

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

    private String buildExplainPriorityPrompt(String context) {
        return "You are a security expert. Based on the following FACTS from the security scanning system, explain why this vulnerability received its priority level.\n\n" +
               context + "\n\n" +
               "IMPORTANT: Your response must clearly distinguish between FACTS from the scanner/system and your AI GUIDANCE.\n\n" +
               "Provide:\n" +
               "1. A simple explanation suitable for non-technical stakeholders\n" +
               "2. A technical explanation for developers\n" +
               "3. Why the priority level is appropriate based on the context\n\n" +
               "Do not invent facts. Only use the information provided above.";
    }

    private String buildRemediationGuidancePrompt(String context) {
        return "You are a security expert. Based on the following FACTS from the security scanning system, provide remediation guidance.\n\n" +
               context + "\n\n" +
               "IMPORTANT: Your response must clearly distinguish between FACTS from the scanner/system and your AI GUIDANCE.\n\n" +
               "Provide:\n" +
               "1. Step-by-step remediation instructions\n" +
               "2. How to verify the fix\n" +
               "3. Potential risks or assumptions\n" +
               "4. Additional security considerations\n\n" +
               "Do not invent facts. Only use the information provided above.";
    }

    private String buildServiceRiskSummaryPrompt(String context) {
        return "You are a security expert. Based on the following FACTS about a service, summarize its security risk profile.\n\n" +
               context + "\n\n" +
               "IMPORTANT: Your response must clearly distinguish between FACTS from the scanner/system and your AI GUIDANCE.\n\n" +
               "Provide:\n" +
               "1. Overall security assessment\n" +
               "2. Top risks that need attention\n" +
               "3. Recommended actions for the service team\n\n" +
               "Do not invent facts. Only use the information provided above.";
    }

    private String buildDailySecurityBriefPrompt(String context) {
        return "You are a security expert. Based on the following FACTS about the current security state, generate a daily security brief.\n\n" +
               context + "\n\n" +
               "IMPORTANT: Your response must clearly distinguish between FACTS from the scanner/system and your AI GUIDANCE.\n\n" +
               "Provide:\n" +
               "1. What requires attention today\n" +
               "2. Which services need focus\n" +
               "3. Summary of recent security changes\n" +
               "4. Recommended priorities for engineering teams\n\n" +
               "Do not invent facts. Only use the information provided above.";
    }

    private String callGeminiApi(String prompt) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + geminiApiKey;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", prompt)
                    ))
                )
            );
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode content = candidates.get(0).path("content");
                    JsonNode parts = content.path("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        return parts.get(0).path("text").asText();
                    }
                }
            }
            
            return "Failed to parse Gemini response";
            
        } catch (Exception e) {
            return "Error calling Gemini API: " + e.getMessage();
        }
    }
}