package com.securityintel.controller;

import com.securityintel.dto.DashboardSummaryDto;
import com.securityintel.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        DashboardSummaryDto summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/action-center")
    public ResponseEntity<DashboardService.ActionCenterDashboardDto> getActionCenterDashboard() {
        DashboardService.ActionCenterDashboardDto dashboard = dashboardService.getActionCenterDashboard();
        return ResponseEntity.ok(dashboard);
    }
}