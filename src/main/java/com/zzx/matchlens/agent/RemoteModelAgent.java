package com.zzx.matchlens.agent;

import com.zzx.matchlens.entity.Match;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class RemoteModelAgent implements AiAgent {

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.base-url}")
    private String baseUrl;

    @Value("${deepseek.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final DataCollectAgent dataCollectAgent;

    public RemoteModelAgent(DataCollectAgent dataCollectAgent) {
        this.dataCollectAgent = dataCollectAgent;
    }

    @Override
    public String execute(Match match) {
        return callDeepSeek(match);
    }

    @SuppressWarnings("unchecked")
    public String callDeepSeek(Match match) {
        String matchData = dataCollectAgent.collect(match);

        String systemPrompt = "你是一名专业的体育赛事分析师。请根据提供的比赛数据，生成一份详细的赛后复盘报告。"
                + "报告应包含：1）比赛结果概述 2）关键球员表现分析 3）关键事件回顾 "
                + "4）胜负原因深度分析 5）针对性改进建议。请使用专业但易懂的中文分析。";

        String userPrompt = "以下是比赛数据，请生成赛后复盘报告：\n\n" + matchData;

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

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "AI 模型返回结果为空，降级使用本地规则生成。";
        } catch (Exception e) {
            System.err.println("[RemoteModelAgent] DeepSeek API 调用失败: " + e.getMessage());
            return null;
        }
    }
}
