package com.zzx.matchlens.controller;

import com.zzx.matchlens.dto.DashboardSummaryVO;
import com.zzx.matchlens.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端仪表盘接口（ADMIN 角色可访问）
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    /** 仪表盘服务，提供汇总统计数据（比赛数、球队数、球员数等） */
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 获取仪表盘汇总统计数据
     */
    @GetMapping("/summary")
    public DashboardSummaryVO getSummary() {
        return dashboardService.getSummary();
    }
}
