package com.zzx.matchlens.controller;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.dto.MatchReportVO;
import com.zzx.matchlens.dto.SaveReportRequest;
import com.zzx.matchlens.service.MatchReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MatchReportController {

    private final MatchReportService matchReportService;

    public MatchReportController(MatchReportService matchReportService) {
        this.matchReportService = matchReportService;
    }

    @GetMapping("/api/matches/{matchId}/reports")
    public List<MatchReportVO> getReports(@PathVariable String matchId) {
        return matchReportService.getReports(matchId);
    }

    @GetMapping("/api/reports/{reportId}")
    public MatchReportVO getReport(@PathVariable String reportId) {
        return matchReportService.getReport(reportId);
    }

    @PostMapping("/api/matches/{matchId}/reports")
    public Result<String> saveReport(@PathVariable String matchId,
                                     @RequestBody SaveReportRequest request) {
        try {
            String reportId = matchReportService.saveReport(matchId, request);
            return Result.ok("报告保存成功", reportId);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/api/matches/{matchId}/reports/generate")
    public Result<String> generateReport(@PathVariable String matchId,
                                         @RequestParam String reportType) {
        try {
            String reportId = matchReportService.generateReport(matchId, reportType);
            return Result.ok("报告生成成功", reportId);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @DeleteMapping("/api/reports/{reportId}")
    public Result<Void> deleteReport(@PathVariable String reportId) {
        try {
            matchReportService.deleteReport(reportId);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
