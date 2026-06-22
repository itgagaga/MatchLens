package com.zzx.matchlens.controller;

import com.zzx.matchlens.entity.MatchStatistics;
import com.zzx.matchlens.service.ReportService;
import com.zzx.matchlens.service.StatisticsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches/{matchId}")
public class ReportController {

    private final StatisticsService statisticsService;
    private final ReportService reportService;

    public ReportController(StatisticsService statisticsService, ReportService reportService) {
        this.statisticsService = statisticsService;
        this.reportService = reportService;
    }

    @GetMapping("/statistics")
    public MatchStatistics getStatistics(@PathVariable String matchId) {
        return statisticsService.getStatistics(matchId);
    }

    @GetMapping("/analysis")
    public String getAnalysis(@PathVariable String matchId) {
        return statisticsService.getAnalysis(matchId);
    }

    @GetMapping("/report")
    public String generateReport(@PathVariable String matchId) {
        return reportService.generateReport(matchId);
    }
}
