package com.securityintel.controller;

import com.securityintel.ai.GeminiAiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

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

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of("configured", geminiAiService.isConfigured(), "mode", geminiAiService.getMode(), "model", geminiAiService.getModel()));
    }

    @PostMapping("/explain-priority")
    public ResponseEntity<String> explainPriority(@RequestBody AiRequest request) {
        require(request.getFindingId(), "findingId");
        String response = geminiAiService.explainPriority(request.getFindingId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/remediation-guidance")
    public ResponseEntity<String> generateRemediationGuidance(@RequestBody AiRequest request) {
        require(request.getFindingId(), "findingId");
        String response = geminiAiService.generateRemediationGuidance(request.getFindingId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/service-risk-summary")
    public ResponseEntity<String> summarizeServiceRisk(@RequestBody AiRequest request) {
        require(request.getServiceId(), "serviceId");
        String response = geminiAiService.summarizeServiceRisk(request.getServiceId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/daily-security-brief")
    public ResponseEntity<String> generateDailySecurityBrief() {
        String response = geminiAiService.generateDailySecurityBrief();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody AiRequest request) {
        require(request.getMessage(), "message");
        return ResponseEntity.ok(geminiAiService.answerQuestion(request.getMessage(), request.getServiceId(), request.getFindingId()));
    }

    private void require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }

    public static class AiRequest {
        private String findingId;
        private String serviceId;
        private String message;

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

        public String getMessage() { return message; }

        public void setMessage(String message) { this.message = message; }
    }
}