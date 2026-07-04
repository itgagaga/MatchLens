package com.zzx.matchlens.agent;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.service.AiCallLogService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Component
public class RemoteModelAgent implements AiAgent {

    /**
     * 保留 apiKey 字段用于运行时校验与降级测试。
     * 实际 AI 调用由 Spring AI ChatClient 处理（api-key 在 Spring AI 自动配置中读取）。
     */
    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${ai.max-retries:2}")
    private int maxRetries;

    private final DataCollectAgent dataCollectAgent;
    private final AiCallLogService aiCallLogService;
    private final ChatClient chatClient;

    public RemoteModelAgent(DataCollectAgent dataCollectAgent,
                            AiCallLogService aiCallLogService,
                            ChatClient.Builder chatClientBuilder) {
        this.dataCollectAgent = dataCollectAgent;
        this.aiCallLogService = aiCallLogService;
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String execute(Match match) {
        AiAgentResponse response = callWithPrompt(match, AiAgentType.REVIEW_REPORT,
                buildDefaultSystemPrompt(), buildUserPrompt(match));
        return response.isSuccess() ? response.getContent() : null;
    }

    /**
     * 使用 Spring AI ChatClient 调用远程 AI 模型（同步调用，带重试）。
     */
    public AiAgentResponse callWithPrompt(Match match, AiAgentType agentType,
                                          String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            AiAgentResponse resp = AiAgentResponse.failure("API Key 未配置", 0, agentType, match.getMatchId());
            logResponse(resp, systemPrompt + "\n---\n" + userPrompt);
            return resp;
        }

        String fullPrompt = systemPrompt + "\n---\n" + userPrompt;
        Exception lastException = null;
        long totalStart = System.currentTimeMillis();

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            long attemptStart = System.currentTimeMillis();
            try {
                String content = chatClient.prompt()
                        .system(systemPrompt)
                        .user(userPrompt)
                        .call()
                        .content();
                long elapsed = System.currentTimeMillis() - attemptStart;

                if (isContentValid(content)) {
                    AiAgentResponse resp = AiAgentResponse.success(content, elapsed, agentType, match.getMatchId());
                    logResponse(resp, fullPrompt);
                    return resp;
                }

                AiAgentResponse resp = AiAgentResponse.failure("AI 模型返回内容为空或无效", elapsed, agentType, match.getMatchId());
                logResponse(resp, fullPrompt);
                return resp;

            } catch (Exception e) {
                lastException = e;
                long elapsed = System.currentTimeMillis() - attemptStart;
                System.err.println("[RemoteModelAgent] 第 " + attempt + " 次调用失败 (" + elapsed + "ms): " + e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        long totalTime = System.currentTimeMillis() - totalStart;
        String errorMsg = "远程 AI 调用失败（重试 " + maxRetries + " 次后放弃）";
        if (lastException != null) {
            errorMsg += ": " + lastException.getMessage();
        }
        AiAgentResponse resp = AiAgentResponse.failure(errorMsg, totalTime, agentType, match.getMatchId());
        logResponse(resp, fullPrompt);
        return resp;
    }

    /**
     * 使用 Spring AI ChatClient 流式调用远程 AI 模型（SSE 流式输出）。
     */
    public AiAgentResponse streamChat(Match match, String fullPrompt, String userPrompt,
                                         AiAgentType agentType, Consumer<String> onChunk) {
        if (apiKey == null || apiKey.isBlank()) {
            AiAgentResponse resp = AiAgentResponse.failure("API Key 未配置", 0, agentType, match.getMatchId());
            logResponse(resp, fullPrompt);
            return resp;
        }

        long start = System.currentTimeMillis();
        StringBuilder collected = new StringBuilder();

        try {
            Flux<String> flux = chatClient.prompt()
                    .system(fullPrompt)
                    .user(userPrompt)
                    .stream()
                    .content();

            // 阻塞消费流式响应，同时收集内容
            flux.doOnNext(chunk -> {
                collected.append(chunk);
                onChunk.accept(chunk);
            }).blockLast();

            long elapsed = System.currentTimeMillis() - start;
            AiAgentResponse resp = AiAgentResponse.success(collected.toString(), elapsed, agentType, match.getMatchId());
            logResponse(resp, fullPrompt);
            return resp;

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            String errorMsg = "流式 AI 调用失败: " + e.getMessage();
            System.err.println("[RemoteModelAgent] streamChat 失败 (" + elapsed + "ms): " + e.getMessage());
            AiAgentResponse resp = AiAgentResponse.failure(errorMsg, elapsed, agentType, match.getMatchId());
            logResponse(resp, fullPrompt);
            return resp;
        }
    }

    private void logResponse(AiAgentResponse response, String prompt) {
        try {
            aiCallLogService.log(response, prompt);
        } catch (Exception e) {
            System.err.println("[RemoteModelAgent] 日志记录失败: " + e.getMessage());
        }
    }

    private boolean isContentValid(String content) {
        if (content == null || content.trim().isEmpty()) return false;
        String[] errorIndicators = {"错误", "error", "Error", "失败", "无法", "API", "api key"};
        for (String indicator : errorIndicators) {
            if (content.contains(indicator) && content.length() < 50) return false;
        }
        return true;
    }

    private String buildDefaultSystemPrompt() {
        return "你是一名专业的体育赛事分析师。请根据提供的比赛数据，生成一份详细的赛后复盘报告。"
                + "报告应包含：1）比赛结果概述 2）关键球员表现分析 3）关键事件回顾 "
                + "4）胜负原因深度分析 5）针对性改进建议。请使用专业但易懂的中文分析。";
    }

    private String buildUserPrompt(Match match) {
        String matchData = dataCollectAgent.collect(match);
        StringBuilder sb = new StringBuilder();
        sb.append("以下是比赛的结构化数据，请基于这些数据进行分析：\n\n");
        sb.append("【赛事类型】").append(match.getSportType()).append("\n");
        sb.append("【比赛状态】").append(match.getStatus()).append("\n");
        if (match.getHomeTeam() != null && match.getAwayTeam() != null) {
            sb.append("【甲方】").append(match.getHomeTeam().getTeamName())
              .append(" （").append(match.getHomeTeam().getScore()).append("分）\n");
            sb.append("【乙方】").append(match.getAwayTeam().getTeamName())
              .append(" （").append(match.getAwayTeam().getScore()).append("分）\n");
        }
        sb.append("【当前时间】").append(LocalDateTime.now()).append("\n\n");
        sb.append("【详细数据】\n").append(matchData);
        return sb.toString();
    }
}
