package com.zzx.matchlens.controller;

import com.zzx.matchlens.agent.AiAgentResponse;
import com.zzx.matchlens.agent.AiAgentType;
import com.zzx.matchlens.agent.DataCollectAgent;
import com.zzx.matchlens.agent.RemoteModelAgent;
import com.zzx.matchlens.common.MatchStatus;
import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.common.SportType;
import com.zzx.matchlens.dto.RecommendMatchRequest;
import com.zzx.matchlens.dto.MatchReportVO;
import com.zzx.matchlens.entity.*;
import com.zzx.matchlens.repository.MatchRepository;
import com.zzx.matchlens.service.EventService;
import com.zzx.matchlens.service.MatchReportService;
import com.zzx.matchlens.service.MatchService;
import com.zzx.matchlens.service.QaMessageService;
import com.zzx.matchlens.service.StatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 客户端比赛浏览接口（USER 角色可访问）
 */
@RestController
@RequestMapping("/api/client/matches")
public class ClientMatchController {

    private static final Logger log = LoggerFactory.getLogger(ClientMatchController.class);
    private final MatchService matchService;                       // 比赛服务，提供比赛 CRUD 和状态管理
    private final RemoteModelAgent remoteModelAgent;               // 远程 AI 模型智能体，用于调用 AI 推荐和问答
    private final DataCollectAgent dataCollectAgent;               // 数据采集智能体，收集比赛结构化数据供 AI 分析
    private final MatchRepository matchRepository;                 // 比赛仓储层，提供比赛实体的数据库访问
    private final EventService eventService;                       // 事件服务，处理比赛事件的查询和筛选
    private final StatisticsService statisticsService;             // 统计服务，提供比赛统计数据查询
    private final MatchReportService matchReportService;           // 报告服务，提供比赛报告的查询
    private final QaMessageService qaMessageService;               // 问答消息服务，管理 AI 问答的历史记录
    private final ExecutorService executor = Executors.newCachedThreadPool(); // 线程池，用于异步执行 SSE 流式 AI 调用

    public ClientMatchController(MatchService matchService,
                                 RemoteModelAgent remoteModelAgent,
                                 DataCollectAgent dataCollectAgent,
                                 MatchRepository matchRepository,
                                 EventService eventService,
                                 StatisticsService statisticsService,
                                 MatchReportService matchReportService,
                                 QaMessageService qaMessageService) {
        this.matchService = matchService;
        this.remoteModelAgent = remoteModelAgent;
        this.dataCollectAgent = dataCollectAgent;
        this.matchRepository = matchRepository;
        this.eventService = eventService;
        this.statisticsService = statisticsService;
        this.matchReportService = matchReportService;
        this.qaMessageService = qaMessageService;
    }

    /**
     * 条件查询比赛列表
     */
    @GetMapping
    public List<Match> queryMatches(
            @RequestParam(required = false) String sportType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {

        List<Match> all = matchService.getAllMatches();

        // 为每场比赛重算统计
        for (Match m : all) {
            if (m.getHomeTeam() != null && m.getAwayTeam() != null) {
                m.getStatistics().updateFromMatch(m);
            }
        }

        // 赛事类型过滤
        if (sportType != null && !sportType.isBlank()) {
            try {
                SportType type = SportType.valueOf(sportType);
                all = all.stream().filter(m -> m.getSportType() == type).collect(Collectors.toList());
            } catch (IllegalArgumentException ignored) {}
        }

        // 比赛状态过滤
        if (status != null && !status.isBlank()) {
            try {
                MatchStatus st = MatchStatus.valueOf(status);
                all = all.stream().filter(m -> m.getStatus() == st).collect(Collectors.toList());
            } catch (IllegalArgumentException ignored) {}
        }

        // 关键词搜索（比赛名称、队伍名称）
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            all = all.stream().filter(m -> {
                if (m.getMatchName() != null && m.getMatchName().toLowerCase().contains(kw)) return true;
                if (m.getHomeTeam() != null && m.getHomeTeam().getTeamName() != null
                        && m.getHomeTeam().getTeamName().toLowerCase().contains(kw)) return true;
                if (m.getAwayTeam() != null && m.getAwayTeam().getTeamName() != null
                        && m.getAwayTeam().getTeamName().toLowerCase().contains(kw)) return true;
                return false;
            }).collect(Collectors.toList());
        }

        return all;
    }

    /**
     * AI 智能推荐比赛
     * @param request 推荐请求，包含用户观赛偏好描述
     * @return 推荐结果列表（含比赛信息、推荐理由、匹配度评分），或失败提示
     */
    @PostMapping("/recommend")
    public Result<?> recommendMatches(@RequestBody RecommendMatchRequest request) {
        // 1. 参数校验：用户偏好不能为空
        if (request.getPreference() == null || request.getPreference().isBlank()) {
            return Result.fail("请输入您的观赛偏好");
        }

        // 2. 获取所有比赛数据
        List<Match> all = matchService.getAllMatches();
        if (all.isEmpty()) {
            return Result.fail("当前没有比赛数据");
        }

        // 3. 为每场比赛重算统计数据，确保比分、分差等字段为最新
        for (Match m : all) {
            if (m.getHomeTeam() != null && m.getAwayTeam() != null) {
                m.getStatistics().updateFromMatch(m);
            }
        }

        // 4. 构建比赛数据摘要文本，供 AI 分析使用
        String matchSummary = buildMatchSummary(all);

        // 5. 构建系统提示词：定义 AI 推荐助手角色、推荐维度和返回格式
        String systemPrompt = "你是 MatchLens 智能赛事推荐助手。用户会描述他们想看的比赛类型、偏好球员或比赛风格，"
                + "你需要从给定的比赛列表中选出最符合用户偏好的比赛（最多推荐5场），并说明推荐理由。\n\n"
                + "推荐时可参考以下维度：\n"
                + "- 比赛类型（篮球/足球/排球等）\n"
                + "- 比赛状态（进行中、已结束等）\n"
                + "- 比分差距与竞争激烈程度\n"
                + "- 特定球员的表现数据（得分、助攻、篮板等）\n\n"
                + "请严格按照以下 JSON 格式返回结果，不要包含其他内容：\n"
                + "```json\n"
                + "[\n"
                + "  {\"matchId\": \"比赛ID\", \"reason\": \"推荐理由\", \"score\": 95},\n"
                + "  ...\n"
                + "]\n"
                + "```\n"
                + "其中 score 为推荐匹配度（0-100），按 score 从高到低排序。"
                + "只返回 JSON 数组，不要包含 markdown 代码块标记或其他文字。";

        // 6. 构建用户提示词：包含所有比赛数据和用户的观赛偏好
        String userPrompt = "以下是当前所有比赛的数据：\n\n" + matchSummary
                + "\n\n用户想看：" + request.getPreference()
                + "\n\n请推荐最符合的比赛。";

        // 7. 同步调用远程 AI 模型进行智能推荐
        AiAgentResponse response = remoteModelAgent.callGeneric(
                systemPrompt, userPrompt, AiAgentType.MATCH_RECOMMENDATION);

        // 8. AI 调用失败时返回错误信息
        if (!response.isSuccess()) {
            return Result.fail("AI 推荐失败：" + response.getErrorMessage());
        }

        // 9. 解析 AI 返回的 JSON 并组装结果
        try {
            String content = response.getContent().trim();
            // 移除 AI 可能包裹的 markdown 代码块标记（如 ```json ... ```）
            if (content.startsWith("```")) {
                content = content.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "");
            }

            // 解析 JSON 数组，获取 AI 推荐的比赛 ID 列表
            List<Map<String, Object>> recommendations = parseRecommendations(content);
            List<Map<String, Object>> result = new ArrayList<>();

            // 遍历 AI 推荐结果，关联比赛实体数据
            for (Map<String, Object> rec : recommendations) {
                String matchId = (String) rec.get("matchId");
                // 根据 AI 返回的 matchId 查找对应的比赛实体
                Match match = all.stream().filter(m -> m.getMatchId().equals(matchId)).findFirst().orElse(null);
                if (match != null) {
                    // 组装返回给前端的推荐结果项
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("matchId", match.getMatchId());
                    item.put("matchName", match.getMatchName());
                    item.put("sportType", match.getSportType());
                    item.put("status", match.getStatus());
                    item.put("teamA", match.getHomeTeam());
                    item.put("teamB", match.getAwayTeam());

                    // 附加比赛统计数据（比分、分差）
                    MatchStatistics stats = match.getStatistics();
                    Map<String, Object> scoreMap = new LinkedHashMap<>();
                    scoreMap.put("scoreA", stats.getHomeScore());
                    scoreMap.put("scoreB", stats.getAwayScore());
                    scoreMap.put("scoreDifference", stats.getScoreDifference());
                    item.put("statistics", scoreMap);

                    // 附加 AI 给出的推荐理由和匹配度评分
                    item.put("reason", rec.getOrDefault("reason", ""));
                    item.put("recommendScore", rec.getOrDefault("score", 0));
                    result.add(item);
                }
            }

            // AI 未匹配到任何有效比赛
            if (result.isEmpty()) {
                return Result.fail("AI 未能匹配到合适的比赛，请尝试调整描述");
            }

            return Result.ok(result);
        } catch (Exception e) {
            // AI 返回格式异常，直接返回原始文本供前端展示
            return Result.ok(Map.of(
                    "rawResponse", response.getContent(),
                    "message", "AI 返回格式异常，以下为原始分析结果"
            ));
        }
    }

    /**
     * 客户端查看单场比赛详情
     */
    @GetMapping("/{matchId}")
    public Match getMatch(@PathVariable String matchId) {
        Match match = matchService.getMatch(matchId);
        if (match.getHomeTeam() != null && match.getAwayTeam() != null) {
            match.getStatistics().updateFromMatch(match);
        }
        return match;
    }

    /**
     * 客户端查看比赛事件时间线
     */
    @GetMapping("/{matchId}/events")
    public Result<List<MatchEvent>> getMatchEvents(@PathVariable String matchId) {
        List<MatchEvent> events = eventService.getMatchEvents(matchId);
        return Result.ok(events);
    }

    /**
     * 客户端查看比赛统计
     */
    @GetMapping("/{matchId}/statistics")
    public MatchStatistics getStatistics(@PathVariable String matchId) {
        return statisticsService.getStatistics(matchId);
    }

    /**
     * AI 赛事问答助手 - SSE 流式响应
     * @param matchId  比赛 ID
     * @param question 用户提出的问题
     * @param request  HTTP 请求对象，用于获取 JWT 中的用户 ID
     * @return SseEmitter SSE 发射器，超时时间 120 秒
     */
    @GetMapping(value = "/{matchId}/qa/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamQa(@PathVariable String matchId, @RequestParam String question,
                               HttpServletRequest request) {
        // 从 JWT Token 中解析当前用户 ID（由 JwtInterceptor 注入）
        String userIdStr = (String) request.getAttribute("userId");
        Long userId = userIdStr != null ? Long.parseLong(userIdStr) : 0L;

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

                // 2. 保存用户提问到问答历史记录
                qaMessageService.saveMessage(matchId, userId, "user", question);

                // 3. 收集比赛结构化数据，构建 AI 问答所需的提示词
                String matchData = dataCollectAgent.collect(match);
                String systemPrompt = buildQaSystemPrompt(match, matchData);
                String userPrompt = question;

                // 4. 发送 start 事件，通知前端流式输出即将开始
                emitter.send(SseEmitter.event().name("start").data(""));

                // 5. 调用远程 AI 模型进行流式对话，每产生一个文本片段即通过 SSE chunk 事件推送
                AiAgentResponse resp = remoteModelAgent.streamChat(match, systemPrompt, userPrompt,
                        AiAgentType.MATCH_QA, chunk -> {
                    try {
                        emitter.send(SseEmitter.event().name("chunk").data(chunk));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                log.info("AI 赛事问答助手");

                // 6. 流式完成后，将 AI 完整回答保存到问答历史记录
                if (resp.isSuccess() && resp.getContent() != null) {
                    qaMessageService.saveMessage(matchId, userId, "ai", resp.getContent());
                }

                // 7. 发送 done 事件，通知前端流式输出结束，并关闭 SSE 连接
                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();
            } catch (Exception e) {
                // 异常处理：尝试发送 error 事件通知前端，然后关闭连接
                try {
                    emitter.send(SseEmitter.event().name("error").data("问答失败: " + e.getMessage()));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    /**
     * 查询问答历史
     */
    @GetMapping("/{matchId}/qa/history")
    public Result<List<QaMessage>> getQaHistory(@PathVariable String matchId, HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        Long userId = userIdStr != null ? Long.parseLong(userIdStr) : 0L;
        List<QaMessage> history = qaMessageService.getHistory(matchId, userId);
        return Result.ok(history);
    }

    /**
     * 清空问答历史
     */
    @DeleteMapping("/{matchId}/qa/history")
    public Result<?> clearQaHistory(@PathVariable String matchId, HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        Long userId = userIdStr != null ? Long.parseLong(userIdStr) : 0L;
        qaMessageService.clearHistory(matchId, userId);
        return Result.ok(null);
    }

    private String buildQaSystemPrompt(Match match, String matchData) {
        String homeName = match.getHomeTeam() != null ? match.getHomeTeam().getTeamName() : "甲方";
        String awayName = match.getAwayTeam() != null ? match.getAwayTeam().getTeamName() : "乙方";
        return "你是 MatchLens 智能赛事问答助手，专门为用户提供关于比赛的专业解答。\n\n"
                + "【比赛信息】\n"
                + "赛事类型：" + match.getSportType() + "\n"
                + "比赛状态：" + match.getStatus() + "\n"
                + "甲方：" + homeName + "\n"
                + "乙方：" + awayName + "\n\n"
                + "【比赛详细数据】\n" + matchData + "\n\n"
                + "【回答要求】\n"
                + "1. **严格基于上述比赛数据回答**，所有数字（得分、助攻、篮板等）必须与提供的数据完全一致，绝对不可编造或推测数据\n"
                + "2. 球员个人得分之和不得超过其所在球队的总比分，回答前请自行校验数据一致性\n"
                + "3. 回答简洁专业，使用中文\n"
                + "4. 涉及数据对比时，适当使用表格或列表展示\n"
                + "5. 对于与比赛数据无关的通用问题（如闲聊、常识、数学等），正常回答即可\n"
                + "6. 如果用户询问的比赛相关数据在当前数据中不存在，诚实说明即可\n"
                + "7. 适当使用 Markdown 格式使回答更清晰\n"
                + "8. 回答重点内容用 **加粗** 标注";
    }

    /**
     * 客户端查看比赛报告列表
     */
    @GetMapping("/{matchId}/reports")
    public List<MatchReportVO> getMatchReports(@PathVariable String matchId) {
        return matchReportService.getReports(matchId);
    }

    /**
     * 构建比赛数据摘要
     */
    private String buildMatchSummary(List<Match> matches) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matches.size(); i++) {
            Match m = matches.get(i);
            sb.append(String.format("【比赛 %d】ID: %s\n", i + 1, m.getMatchId()));
            sb.append("  名称: ").append(m.getMatchName() != null ? m.getMatchName() : "未命名").append("\n");
            sb.append("  类型: ").append(m.getSportType()).append("\n");
            sb.append("  状态: ").append(m.getStatus()).append("\n");

            if (m.getHomeTeam() != null && m.getAwayTeam() != null) {
                sb.append("  甲方: ").append(m.getHomeTeam().getTeamName());
                sb.append(" vs 乙方: ").append(m.getAwayTeam().getTeamName()).append("\n");

                MatchStatistics stats = m.getStatistics();
                sb.append("  比分: ").append(stats.getHomeScore()).append(" : ").append(stats.getAwayScore()).append("\n");
                sb.append("  分差: ").append(stats.getScoreDifference()).append("\n");
                if (stats.getLeadingTeam() != null) {
                    sb.append("  领先方: ").append(stats.getLeadingTeam()).append("\n");
                }
                sb.append("  事件数: ").append(stats.getEventCount()).append("\n");

                // 追加球员数据
                appendTeamPlayers(sb, "  甲方球员", m.getHomeTeam());
                appendTeamPlayers(sb, "  乙方球员", m.getAwayTeam());
            } else {
                sb.append("  队伍: 未设置\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 追加球队球员及其核心数据
     */
    private void appendTeamPlayers(StringBuilder sb, String label, Team team) {
        if (team == null || team.getPlayers() == null || team.getPlayers().isEmpty()) return;
        sb.append(label).append(":\n");
        for (Player p : team.getPlayers()) {
            sb.append("    - ").append(p.getPlayerName());
            if (p.getNumber() > 0) sb.append(" (#").append(p.getNumber()).append(")");
            Map<String, Integer> stats = p.getStatistics();
            if (stats != null && !stats.isEmpty()) {
                sb.append(" [");
                boolean first = true;
                for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                    if (!first) sb.append(", ");
                    sb.append(entry.getKey()).append(":").append(entry.getValue());
                    first = false;
                }
                sb.append("]");
            }
            sb.append("\n");
        }
    }

    /**
     * 简单解析 AI 返回的 JSON 数组
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseRecommendations(String json) {
        // 使用 Jackson 解析
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, List.class);
        } catch (Exception e) {
            // 尝试提取 JSON 数组部分
            int start = json.indexOf('[');
            int end = json.lastIndexOf(']');
            if (start >= 0 && end > start) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    return mapper.readValue(json.substring(start, end + 1), List.class);
                } catch (Exception ignored) {}
            }
            return Collections.emptyList();
        }
    }
}
