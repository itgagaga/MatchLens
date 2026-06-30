package com.zzx.matchlens.controller;

import com.zzx.matchlens.dto.DashboardSummaryVO;
import com.zzx.matchlens.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryVO getSummary() {
        return dashboardService.getSummary();
    }
}
