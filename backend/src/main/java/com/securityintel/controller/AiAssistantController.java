package com.securityintel.controller;

import com.securityintel.ai.GeminiAiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai-assistant")
@CrossOrigin(origins = "*")
public class AiAssistantController {

    private final GeminiAiService geminiAiService;

    public AiAssistantController(GeminiAiService geminiAiService) {
        this.geminiAiService = geminiAiService;
    }

    @GetMapping("/configured")
    public ResponseEntity<Boolean> isConfigured() {
        return ResponseEntity.ok(geminiAiService.isConfigured());
    }

    @PostMapping("/explain-priority")
    public ResponseEntity<String> explainPriority(@RequestBody AiRequest request) {
        String response = geminiAiService.explainPriority(request.getFindingId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/remediation-guidance")
    public ResponseEntity<String> generateRemediationGuidance(@RequestBody AiRequest request) {
        String response = geminiAiService.generateRemediationGuidance(request.getFindingId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/service-risk-summary")
    public ResponseEntity<String> summarizeServiceRisk(@RequestBody AiRequest request) {
        String response = geminiAiService.summarizeServiceRisk(request.getServiceId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/daily-security-brief")
    public ResponseEntity<String> generateDailySecurityBrief() {
        String response = geminiAiService.generateDailySecurityBrief();
        return ResponseEntity.ok(response);
    }

    public static class AiRequest {
        private String findingId;
        private String serviceId;

        public String getFindingId() {
            return findingId;
        }

        public void setFindingId(String findingId) {
            this.findingId = findingId;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }
    }
}