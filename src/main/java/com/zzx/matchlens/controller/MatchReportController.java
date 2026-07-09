package com.zzx.matchlens.controller;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.dto.MatchReportVO;
import com.zzx.matchlens.dto.SaveReportRequest;
import com.zzx.matchlens.service.MatchReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端报告管理接口（ADMIN 角色可访问）
 */
@RestController
public class MatchReportController {

    /** 报告服务，提供报告的查询、保存、AI 生成和删除功能 */
    private final MatchReportService matchReportService;

    public MatchReportController(MatchReportService matchReportService) {
        this.matchReportService = matchReportService;
    }

    /**
     * 获取指定比赛的报告列表
     */
    @GetMapping("/api/matches/{matchId}/reports")
    public List<MatchReportVO> getReports(@PathVariable String matchId) {
        return matchReportService.getReports(matchId);
    }

    /**
     * 获取单份报告详情
     */
    @GetMapping("/api/reports/{reportId}")
    public MatchReportVO getReport(@PathVariable String reportId) {
        return matchReportService.getReport(reportId);
    }

    /**
     * 保存手动创建的报告
     */
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

    /**
     * AI 自动生成报告
     */
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

    /**
     * 删除指定报告
     */
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
