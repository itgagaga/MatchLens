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

    private final MatchService matchService;
    private final RemoteModelAgent remoteModelAgent;
    private final DataCollectAgent dataCollectAgent;
    private final MatchRepository matchRepository;
    private final EventService eventService;
    private final StatisticsService statisticsService;
    private final MatchReportService matchReportService;
    private final QaMessageService qaMessageService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

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
     */
    @PostMapping("/recommend")
    public Result<?> recommendMatches(@RequestBody RecommendMatchRequest request) {
        if (request.getPreference() == null || request.getPreference().isBlank()) {
            return Result.fail("请输入您的观赛偏好");
        }

        List<Match> all = matchService.getAllMatches();
        if (all.isEmpty()) {
            return Result.fail("当前没有比赛数据");
        }

        // 为每场比赛重算统计
        for (Match m : all) {
            if (m.getHomeTeam() != null && m.getAwayTeam() != null) {
                m.getStatistics().updateFromMatch(m);
            }
        }

        // 构建比赛数据摘要供 AI 分析
        String matchSummary = buildMatchSummary(all);

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

        String userPrompt = "以下是当前所有比赛的数据：\n\n" + matchSummary
                + "\n\n用户想看：" + request.getPreference()
                + "\n\n请推荐最符合的比赛。";

        AiAgentResponse response = remoteModelAgent.callGeneric(
                systemPrompt, userPrompt, AiAgentType.MATCH_RECOMMENDATION);

        if (!response.isSuccess()) {
            return Result.fail("AI 推荐失败：" + response.getErrorMessage());
        }

        // 解析 AI 返回的 JSON
        try {
            String content = response.getContent().trim();
            // 移除可能的 markdown 代码块标记
            if (content.startsWith("```")) {
                content = content.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "");
            }

            // 简单解析 JSON 数组
            List<Map<String, Object>> recommendations = parseRecommendations(content);
            List<Map<String, Object>> result = new ArrayList<>();

            for (Map<String, Object> rec : recommendations) {
                String matchId = (String) rec.get("matchId");
                Match match = all.stream().filter(m -> m.getMatchId().equals(matchId)).findFirst().orElse(null);
                if (match != null) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("matchId", match.getMatchId());
                    item.put("matchName", match.getMatchName());
                    item.put("sportType", match.getSportType());
                    item.put("status", match.getStatus());
                    item.put("teamA", match.getHomeTeam());
                    item.put("teamB", match.getAwayTeam());

                    MatchStatistics stats = match.getStatistics();
                    Map<String, Object> scoreMap = new LinkedHashMap<>();
                    scoreMap.put("scoreA", stats.getHomeScore());
                    scoreMap.put("scoreB", stats.getAwayScore());
                    scoreMap.put("scoreDifference", stats.getScoreDifference());
                    item.put("statistics", scoreMap);

                    item.put("reason", rec.getOrDefault("reason", ""));
                    item.put("recommendScore", rec.getOrDefault("score", 0));
                    result.add(item);
                }
            }

            if (result.isEmpty()) {
                return Result.fail("AI 未能匹配到合适的比赛，请尝试调整描述");
            }

            return Result.ok(result);
        } catch (Exception e) {
            // AI 返回格式异常，直接返回原始文本
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
     */
    @GetMapping(value = "/{matchId}/qa/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamQa(@PathVariable String matchId, @RequestParam String question,
                               HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        Long userId = userIdStr != null ? Long.parseLong(userIdStr) : 0L;

        SseEmitter emitter = new SseEmitter(120000L);
        executor.execute(() -> {
            try {
                Match match = matchRepository.findById(matchId).orElse(null);
                if (match == null) {
                    emitter.send(SseEmitter.event().name("error").data("比赛不存在"));
                    emitter.complete();
                    return;
                }

                // 保存用户提问
                qaMessageService.saveMessage(matchId, userId, "user", question);

                String matchData = dataCollectAgent.collect(match);
                String systemPrompt = buildQaSystemPrompt(match, matchData);
                String userPrompt = question;

                emitter.send(SseEmitter.event().name("start").data(""));

                AiAgentResponse resp = remoteModelAgent.streamChat(match, systemPrompt, userPrompt,
                        AiAgentType.MATCH_QA, chunk -> {
                    try {
                        emitter.send(SseEmitter.event().name("chunk").data(chunk));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                // 保存 AI 回答
                if (resp.isSuccess() && resp.getContent() != null) {
                    qaMessageService.saveMessage(matchId, userId, "ai", resp.getContent());
                }

                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();
            } catch (Exception e) {
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
