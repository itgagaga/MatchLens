package com.zzx.matchlens.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zzx.matchlens.agent.ReviewReportAgent;
import com.zzx.matchlens.agent.SituationAnalysisAgent;
import com.zzx.matchlens.dto.MatchReportVO;
import com.zzx.matchlens.dto.SaveReportRequest;
import com.zzx.matchlens.entity.AiCallLog;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchReport;
import com.zzx.matchlens.mapper.AiCallLogMapper;
import com.zzx.matchlens.mapper.MatchReportMapper;
import com.zzx.matchlens.repository.MatchRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MatchReportService {

    private final MatchReportMapper matchReportMapper;
    private final MatchRepository matchRepository;
    private final SituationAnalysisAgent situationAnalysisAgent;
    private final ReviewReportAgent reviewReportAgent;
    private final AiCallLogMapper aiCallLogMapper;

    public MatchReportService(MatchReportMapper matchReportMapper,
                              MatchRepository matchRepository,
                              SituationAnalysisAgent situationAnalysisAgent,
                              ReviewReportAgent reviewReportAgent,
                              AiCallLogMapper aiCallLogMapper) {
        this.matchReportMapper = matchReportMapper;
        this.matchRepository = matchRepository;
        this.situationAnalysisAgent = situationAnalysisAgent;
        this.reviewReportAgent = reviewReportAgent;
        this.aiCallLogMapper = aiCallLogMapper;
    }

    public List<MatchReportVO> getReports(String matchId) {
        QueryWrapper<MatchReport> qw = new QueryWrapper<>();
        qw.eq("match_id", matchId).orderByDesc("create_time");
        return matchReportMapper.selectList(qw).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    public MatchReportVO getReport(String reportId) {
        MatchReport report = matchReportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("报告不存在: " + reportId);
        }
        return toVO(report);
    }

    public String saveReport(String matchId, SaveReportRequest request) {
        matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("比赛不存在: " + matchId));

        if (!"SITUATION".equals(request.getReportType()) && !"REVIEW".equals(request.getReportType())) {
            throw new RuntimeException("reportType 只能是 SITUATION 或 REVIEW");
        }

        // 删除同类型的旧报告（新报告覆盖旧报告）
        deleteReportsByType(matchId, request.getReportType());

        MatchReport report = new MatchReport();
        report.setReportId(UUID.randomUUID().toString());
        report.setMatchId(matchId);
        report.setReportType(request.getReportType());
        report.setTitle(request.getTitle());
        report.setContent(request.getContent());
        report.setGeneratedBy(request.getGeneratedBy());
        report.setCreateTime(LocalDateTime.now());

        matchReportMapper.insert(report);
        return report.getReportId();
    }

    public String generateReport(String matchId, String reportType) {
        if (!"SITUATION".equals(reportType) && !"REVIEW".equals(reportType)) {
            throw new RuntimeException("reportType 只能是 SITUATION 或 REVIEW");
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("比赛不存在: " + matchId));

        if (match.getHomeTeam() == null || match.getAwayTeam() == null) {
            throw new RuntimeException("比赛尚未设置队伍，无法生成报告");
        }

        String content;
        String agentType;
        String titlePrefix;

        if ("SITUATION".equals(reportType)) {
            content = situationAnalysisAgent.execute(match);
            agentType = "SITUATION_ANALYSIS";
            titlePrefix = "实时态势分析";
        } else {
            content = reviewReportAgent.execute(match);
            agentType = "REVIEW_REPORT";
            titlePrefix = "赛后复盘报告";
        }

        String generatedBy = checkGeneratedBy(matchId, agentType);

        // 删除同类型的旧报告（新报告覆盖旧报告）
        deleteReportsByType(matchId, reportType);

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        MatchReport report = new MatchReport();
        report.setReportId(UUID.randomUUID().toString());
        report.setMatchId(matchId);
        report.setReportType(reportType);
        report.setTitle(titlePrefix + " - " + now);
        report.setContent(content);
        report.setGeneratedBy(generatedBy);
        report.setCreateTime(LocalDateTime.now());

        matchReportMapper.insert(report);
        return report.getReportId();
    }

    public void deleteReport(String reportId) {
        MatchReport report = matchReportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("报告不存在: " + reportId);
        }
        matchReportMapper.deleteById(reportId);
    }

    /**
     * 删除指定比赛的指定类型的所有报告（用于覆盖更新）
     */
    private void deleteReportsByType(String matchId, String reportType) {
        try {
            QueryWrapper<MatchReport> qw = new QueryWrapper<>();
            qw.eq("match_id", matchId).eq("report_type", reportType);
            matchReportMapper.delete(qw);
        } catch (Exception e) {
            System.err.println("[MatchReportService] 删除旧报告失败: " + e.getMessage());
        }
    }

    private MatchReportVO toVO(MatchReport report) {
        MatchReportVO vo = new MatchReportVO();
        vo.setReportId(report.getReportId());
        vo.setMatchId(report.getMatchId());
        vo.setReportType(report.getReportType());
        vo.setTitle(report.getTitle());
        vo.setContent(report.getContent());
        vo.setGeneratedBy(report.getGeneratedBy());
        vo.setCreateTime(report.getCreateTime());
        return vo;
    }

    private String checkGeneratedBy(String matchId, String agentType) {
        try {
            QueryWrapper<AiCallLog> qw = new QueryWrapper<>();
            qw.eq("match_id", matchId)
              .eq("agent_type", agentType)
              .orderByDesc("call_time")
              .last("LIMIT 1");
            AiCallLog latest = aiCallLogMapper.selectOne(qw);
            if (latest != null && latest.getSuccess() != null && latest.getSuccess() == 1) {
                return "REMOTE_AI";
            }
        } catch (Exception e) {
            System.err.println("[MatchReportService] 查询 AI 调用日志失败: " + e.getMessage());
        }
        return "LOCAL_RULE";
    }
}
