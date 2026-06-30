package com.zzx.matchlens.agent;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.service.AiCallLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class RemoteModelAgent implements AiAgent {

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.base-url}")
    private String baseUrl;

    @Value("${deepseek.model}")
    private String model;

    @Value("${deepseek.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${deepseek.read-timeout:60000}")
    private int readTimeout;

    @Value("${deepseek.max-retries:2}")
    private int maxRetries;

    private final DataCollectAgent dataCollectAgent;
    private final AiCallLogService aiCallLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private RestTemplate restTemplate;

    public RemoteModelAgent(DataCollectAgent dataCollectAgent, AiCallLogService aiCallLogService) {
        this.dataCollectAgent = dataCollectAgent;
        this.aiCallLogService = aiCallLogService;
    }

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                    new org.springframework.http.client.SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(connectTimeout);
            factory.setReadTimeout(readTimeout);
            restTemplate = new RestTemplate(factory);
        }
        return restTemplate;
    }

    /**
     * 测试用：注入自定义 RestTemplate（如模拟超时）
     */
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String execute(Match match) {
        AiAgentResponse response = callWithPrompt(match, AiAgentType.REVIEW_REPORT, buildDefaultSystemPrompt());
        return response.isSuccess() ? response.getContent() : null;
    }

    public AiAgentResponse callWithPrompt(Match match, AiAgentType agentType, String systemPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            AiAgentResponse resp = AiAgentResponse.failure("API Key 未配置", 0, agentType, match.getMatchId());
            logResponse(resp, systemPrompt);
            return resp;
        }

        String matchData = dataCollectAgent.collect(match);
        String userPrompt = buildUserPrompt(match, matchData);
        String fullPrompt = systemPrompt + "\n---\n" + userPrompt;

        Exception lastException = null;
        long totalStart = System.currentTimeMillis();

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            long attemptStart = System.currentTimeMillis();
            try {
                String url = baseUrl + "/chat/completions";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                Map<String, Object> body = Map.of(
                        "model", model,
                        "messages", List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content", userPrompt)
                        ),
                        "temperature", 0.7,
                        "max_tokens", 2000
                );

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

                @SuppressWarnings("unchecked")
                ResponseEntity<Map> response = getRestTemplate().exchange(url, HttpMethod.POST, request, Map.class);
                long elapsed = System.currentTimeMillis() - attemptStart;

                Map<String, Object> responseBody = response.getBody();
                String content = extractContent(responseBody);

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
                    try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                }
            }
        }

        long totalTime = System.currentTimeMillis() - totalStart;
        String errorMsg = "远程 AI 调用失败（重试 " + maxRetries + " 次后放弃）";
        if (lastException != null) {
            if (lastException.getCause() instanceof SocketTimeoutException) {
                errorMsg = "远程 AI 调用超时";
            } else {
                errorMsg += ": " + lastException.getMessage();
            }
        }
        AiAgentResponse resp = AiAgentResponse.failure(errorMsg, totalTime, agentType, match.getMatchId());
        logResponse(resp, fullPrompt);
        return resp;
    }

    @SuppressWarnings("unchecked")
    public AiAgentResponse streamChat(Match match, String systemPrompt, Consumer<String> onChunk) {
        if (apiKey == null || apiKey.isBlank()) {
            return AiAgentResponse.failure("API Key 未配置", 0, AiAgentType.SITUATION_ANALYSIS, match.getMatchId());
        }

        String matchData = dataCollectAgent.collect(match);
        String userPrompt = buildUserPrompt(match, matchData);
        long start = System.currentTimeMillis();

        try {
            String url = baseUrl + "/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.7,
                    "max_tokens", 2000,
                    "stream", true
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            org.springframework.http.client.SimpleClientHttpRequestFactory streamFactory =
                    new org.springframework.http.client.SimpleClientHttpRequestFactory();
            streamFactory.setConnectTimeout(connectTimeout);
            streamFactory.setReadTimeout(readTimeout * 3);
            RestTemplate streamRestTemplate = new RestTemplate(streamFactory);

            ResponseEntity<org.springframework.core.io.Resource> response = streamRestTemplate.exchange(
                    url, HttpMethod.POST, request, org.springframework.core.io.Resource.class);

            org.springframework.core.io.Resource resource = response.getBody();
            if (resource == null) {
                return AiAgentResponse.failure("流式响应为空", System.currentTimeMillis() - start,
                        AiAgentType.SITUATION_ANALYSIS, match.getMatchId());
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) continue;
                    String data = line.substring(5).trim();
                    if ("[DONE]".equals(data)) break;

                    try {
                        Map<String, Object> chunk = objectMapper.readValue(data, Map.class);
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
                        if (choices != null && !choices.isEmpty()) {
                            Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                            if (delta != null && delta.containsKey("content")) {
                                String content = (String) delta.get("content");
                                if (content != null) {
                                    onChunk.accept(content);
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            long elapsed = System.currentTimeMillis() - start;
            return AiAgentResponse.success("[streamed]", elapsed, AiAgentType.SITUATION_ANALYSIS, match.getMatchId());

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            String errorMsg = "流式 AI 调用失败: " + e.getMessage();
            if (e.getCause() instanceof SocketTimeoutException) {
                errorMsg = "流式 AI 调用超时";
            }
            System.err.println("[RemoteModelAgent] streamChat 失败 (" + elapsed + "ms): " + e.getMessage());
            return AiAgentResponse.failure(errorMsg, elapsed, AiAgentType.SITUATION_ANALYSIS, match.getMatchId());
        }
    }

    private void logResponse(AiAgentResponse response, String prompt) {
        try {
            aiCallLogService.log(response, prompt);
        } catch (Exception e) {
            System.err.println("[RemoteModelAgent] 日志记录失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> responseBody) {
        if (responseBody == null || !responseBody.containsKey("choices")) return null;
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        if (choices == null || choices.isEmpty()) return null;
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) return null;
        return (String) message.get("content");
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

    private String buildUserPrompt(Match match, String matchData) {
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
        sb.append("【当前时间】").append(java.time.LocalDateTime.now()).append("\n\n");
        sb.append("【详细数据】\n").append(matchData);
        return sb.toString();
    }
}
