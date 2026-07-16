package com.zzx.matchlens.controller;

import com.zzx.matchlens.agent.AiAgentResponse;
import com.zzx.matchlens.agent.AiAgentType;
import com.zzx.matchlens.agent.DataCollectAgent;
import com.zzx.matchlens.agent.RemoteModelAgent;
import com.zzx.matchlens.dto.SaveReportRequest;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchStatistics;
import com.zzx.matchlens.repository.MatchRepository;
import com.zzx.matchlens.service.MatchReportService;
import com.zzx.matchlens.service.ReportService;
import com.zzx.matchlens.service.StatisticsService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 管理端报告生成接口（ADMIN 角色可访问，支持 SSE 流式输出）
 */
@RestController
@RequestMapping("/api/matches/{matchId}")
public class ReportController {

    private final StatisticsService statisticsService;             // 统计服务，提供比赛统计数据和分析文本查询
    private final ReportService reportService;                     // 报告服务，生成比赛报告文本
    private final MatchRepository matchRepository;                 // 比赛仓储层，提供比赛实体的数据库访问
    private final RemoteModelAgent remoteModelAgent;               // 远程 AI 模型智能体，用于 SSE 流式生成报告
    private final DataCollectAgent dataCollectAgent;               // 数据采集智能体，收集比赛结构化数据供 AI 分析
    private final MatchReportService matchReportService;           // 报告管理服务，将 AI 生成的报告持久化到数据库
    private final ExecutorService executor = Executors.newCachedThreadPool(); // 线程池，用于异步执行 SSE 流式 AI 调用

    public ReportController(StatisticsService statisticsService,
                            ReportService reportService,
                            MatchRepository matchRepository,
                            RemoteModelAgent remoteModelAgent,
                            DataCollectAgent dataCollectAgent,
                            MatchReportService matchReportService) {
        this.statisticsService = statisticsService;
        this.reportService = reportService;
        this.matchRepository = matchRepository;
        this.remoteModelAgent = remoteModelAgent;
        this.dataCollectAgent = dataCollectAgent;
        this.matchReportService = matchReportService;
    }

    /**
     * 获取比赛统计数据
     */
    @GetMapping("/statistics")
    public MatchStatistics getStatistics(@PathVariable String matchId) {
        return statisticsService.getStatistics(matchId);
    }

    /**
     * 获取比赛分析文本
     */
    @GetMapping("/analysis")
    public String getAnalysis(@PathVariable String matchId) {
        return statisticsService.getAnalysis(matchId);
    }

    /**
     * 生成比赛报告文本
     */
    @GetMapping("/report")
    public String generateReport(@PathVariable String matchId) {
        return reportService.generateReport(matchId);
    }

    /**
     * 流式生成实时态势分析报告（SSE）
     * @return SseEmitter SSE 发射器，超时时间 120 秒
     */
    @GetMapping(value = "/analysis/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAnalysis(@PathVariable String matchId) {
        // 创建 SSE 发射器，设置超时时间为 120 秒
        SseEmitter emitter = new SseEmitter(120000L);
        // 将流式生成任务提交到线程池异步执行，避免阻塞主线程
        executor.execute(() -> {
            try {
                // 1. 根据 matchId 查询比赛实体
                Match match = matchRepository.findById(matchId).orElse(null);
                if (match == null) {
                    // 比赛不存在，发送 error 事件并结束
                    emitter.send(SseEmitter.event().name("error").data("比赛不存在"));
                    emitter.complete();
                    return;
                }

                // 2. 构建 AI 分析所需的系统提示词和用户数据提示词
                String systemPrompt = buildAnalysisPrompt(match);
                String userPrompt = dataCollectAgent.collect(match);

                // 3. 发送 start 事件，通知前端流式输出即将开始
                emitter.send(SseEmitter.event().name("start").data(""));

                // 4. 调用远程 AI 模型进行流式对话，每产生一个文本片段即通过 SSE chunk 事件推送
                AiAgentResponse resp = remoteModelAgent.streamChat(match, systemPrompt, userPrompt,
                        AiAgentType.SITUATION_ANALYSIS, chunk -> {
                    try {
                        emitter.send(SseEmitter.event().name("chunk").data(chunk));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                // 5. 流式完成后，将 AI 生成的完整报告保存到数据库
                if (resp.isSuccess() && resp.getContent() != null) {
                    try {
                        SaveReportRequest req = new SaveReportRequest();
                        req.setReportType("SITUATION");  // 报告类型：实时态势分析
                        req.setTitle("实时态势分析 - " + java.time.LocalDateTime.now().format(
                                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                        req.setContent(resp.getContent());
                        req.setGeneratedBy(resp.isSuccess() ? "REMOTE_AI" : "LOCAL_RULE");
                        matchReportService.saveReport(matchId, req);
                    } catch (Exception e) {
                        System.err.println("[ReportController] 保存态势分析报告失败: " + e.getMessage());
                    }
                }

                // 6. 发送 done 事件，通知前端流式输出结束，并关闭 SSE 连接
                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();
            } catch (Exception e) {
                // 异常处理：尝试发送 error 事件通知前端，然后关闭连接
                try {
                    emitter.send(SseEmitter.event().name("error").data("生成失败: " + e.getMessage()));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    /**
     * 流式生成赛后复盘报告（SSE）
     * @param matchId 比赛 ID
     * @return SseEmitter SSE 发射器，超时时间 120 秒
     */
    @GetMapping(value = "/report/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamReport(@PathVariable String matchId) {
        // 创建 SSE 发射器，设置超时时间为 120 秒
        SseEmitter emitter = new SseEmitter(120000L);
        // 将流式生成任务提交到线程池异步执行，避免阻塞主线程
        executor.execute(() -> {
            try {
                // 1. 根据 matchId 查询比赛实体
                Match match = matchRepository.findById(matchId).orElse(null);
                if (match == null) {
                    // 比赛不存在，发送 error 事件并结束
                    emitter.send(SseEmitter.event().name("error").data("比赛不存在"));
                    emitter.complete();
                    return;
                }

                // 2. 构建 AI 复盘分析所需的系统提示词和用户数据提示词
                String systemPrompt = buildReportPrompt(match);
                String userPrompt = dataCollectAgent.collect(match);

                // 3. 发送 start 事件，通知前端流式输出即将开始
                emitter.send(SseEmitter.event().name("start").data(""));

                // 4. 调用远程 AI 模型进行流式对话，每产生一个文本片段即通过 SSE chunk 事件推送
                AiAgentResponse resp = remoteModelAgent.streamChat(match, systemPrompt, userPrompt,
                        AiAgentType.REVIEW_REPORT, chunk -> {
                    try {
                        emitter.send(SseEmitter.event().name("chunk").data(chunk));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                // 5. 流式完成后，将 AI 生成的完整复盘报告保存到数据库
                if (resp.isSuccess() && resp.getContent() != null) {
                    try {
                        SaveReportRequest req = new SaveReportRequest();
                        req.setReportType("REVIEW");  // 报告类型：赛后复盘报告
                        req.setTitle("赛后复盘报告 - " + java.time.LocalDateTime.now().format(
                                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                        req.setContent(resp.getContent());
                        req.setGeneratedBy(resp.isSuccess() ? "REMOTE_AI" : "LOCAL_RULE");
                        matchReportService.saveReport(matchId, req);
                    } catch (Exception e) {
                        System.err.println("[ReportController] 保存复盘报告失败: " + e.getMessage());
                    }
                }

                // 6. 发送 done 事件，通知前端流式输出结束，并关闭 SSE 连接
                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();
            } catch (Exception e) {
                // 异常处理：尝试发送 error 事件通知前端，然后关闭连接
                try {
                    emitter.send(SseEmitter.event().name("error").data("生成失败: " + e.getMessage()));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    /**
     * 构建实时态势分析的 AI 系统提示词（System Prompt）
     */
    private String buildAnalysisPrompt(Match match) {
        // 拼接完整的系统提示词，由三部分组成：角色定位、赛事信息、输出格式约束
        return "你是一名专业的体育赛事实时态势分析师。请根据以下比赛数据，生成实时态势分析报告。\n\n"
                // 第一部分：角色定位与分析要求，定义 AI 需覆盖的 5 个分析维度
                + "分析要求：\n"
                + "1. 当前比分和比赛走势\n"
                + "2. 双方队伍的优势与劣势\n"
                + "3. 关键球员的实时表现评价\n"
                + "4. 比赛关键时刻和转折点分析\n"
                + "5. 后续比赛走势预测和战术建议\n\n"
                // 第二部分：赛事信息，从 Match 实体中动态提取比赛上下文数据
                + "【赛事信息】\n"
                + "赛事类型：" + match.getSportType() + "\n"           // 运动类型（如篮球、足球）
                + "比赛状态：" + match.getStatus() + "\n"               // 当前比赛状态（进行中/已结束等）
                + "甲方：" + (match.getHomeTeam() != null ? match.getHomeTeam().getTeamName() : "未知") + "\n"  // 主队名称，空值时显示"未知"
                + "乙方：" + (match.getAwayTeam() != null ? match.getAwayTeam().getTeamName() : "未知") + "\n"  // 客队名称，空值时显示"未知"
                + "当前时间：" + java.time.LocalDateTime.now() + "\n\n" // 实时时间，体现态势分析的时效性
                // 第三部分：输出格式约束，要求 AI 严格使用 Markdown 语法输出结构化报告
                + "输出格式要求（严格遵守）：\n"
                + "- 必须使用 Markdown 语法\n"
                + "- 每个分析维度必须用 ## 二级标题，例如：## 1. 当前比分和比赛走势\n"
                + "- 每个要点用 - 无序列表开头\n"
                + "- 重点内容用 **加粗**\n"
                + "- 标题和正文之间必须有空行\n"
                + "- 使用专业但易懂的中文进行分析。";
    }

    /**
     * 构建赛后复盘报告的 AI 系统提示词（System Prompt）
     *
     * @param match 比赛实体，提供赛事类型、状态、主客队等信息用于填充提示词
     * @return 拼接完成的系统提示词字符串
     */
    private String buildReportPrompt(Match match) {
        // 拼接完整的系统提示词，由三部分组成：角色定位、赛事信息、输出格式约束
        return "你是一名专业的体育赛事分析师。请根据提供的比赛数据，生成一份详细的赛后复盘报告。\n\n"
                // 第一部分：角色定位与报告要求，定义 AI 需覆盖的 5 个报告板块
                + "报告应包含：\n"
                + "1）比赛结果概述\n"
                + "2）关键球员表现分析\n"
                + "3）关键事件回顾\n"
                + "4）胜负原因深度分析\n"
                + "5）针对性改进建议\n\n"
                // 第二部分：赛事信息，从 Match 实体中动态提取比赛上下文数据
                + "【赛事信息】\n"
                + "赛事类型：" + match.getSportType() + "\n"           // 运动类型（如篮球、足球）
                + "比赛状态：" + match.getStatus() + "\n"               // 比赛最终状态
                + "甲方：" + (match.getHomeTeam() != null ? match.getHomeTeam().getTeamName() : "未知") + "\n"  // 主队名称，空值时显示"未知"
                + "乙方：" + (match.getAwayTeam() != null ? match.getAwayTeam().getTeamName() : "未知") + "\n\n" // 客队名称，空值时显示"未知"
                // 第三部分：输出格式约束，要求 AI 严格使用 Markdown 语法输出结构化报告
                + "输出格式要求（严格遵守）：\n"
                + "- 必须使用 Markdown 语法\n"
                + "- 每个报告板块必须用 ## 二级标题，例如：## 1. 比赛结果概述\n"
                + "- 每个要点用 - 无序列表开头\n"
                + "- 重点内容用 **加粗**\n"
                + "- 标题和正文之间必须有空行\n"
                + "- 使用专业但易懂的中文分析。";
    }
}
