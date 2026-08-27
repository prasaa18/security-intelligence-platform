package com.securityintel.controller;

import com.securityintel.service.SecurityNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    private final SecurityNotificationService notificationService;

    public NotificationController(SecurityNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/daily-brief")
    public ResponseEntity<Map<String, Object>> sendDailyBrief() {
        return ResponseEntity.ok(Map.of("sent", notificationService.sendDailyBrief()));
    }
}