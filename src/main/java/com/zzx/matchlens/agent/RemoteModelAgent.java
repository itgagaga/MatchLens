package com.zzx.matchlens.agent;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.service.AiCallLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * 远程 AI 模型智能体。
 * <p>
 * 通过 Spring AI ChatClient 调用远程大语言模型，支持同步调用（带重试）和
 * SSE 流式输出两种模式。还记录每次调用的日志，并在 API Key 未配置时提前失败。
 * 是所有依赖远程 AI 的上层 Agent（如 {@link ReviewReportAgent}、
 * {@link SituationAnalysisAgent}）的核心调用引擎。
 * </p>
 */
@Component
public class RemoteModelAgent implements AiAgent {

    private static final Logger log = LoggerFactory.getLogger(RemoteModelAgent.class);
    /**
     * 保留 apiKey 字段用于运行时校验与降级测试。
     * 实际 AI 调用由 Spring AI ChatClient 处理（api-key 在 Spring AI 自动配置中读取）。
     */
    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    /** 最大重试次数，默认 2 次 */
    @Value("${ai.max-retries:2}")
    private int maxRetries;

    private final DataCollectAgent dataCollectAgent;
    private final AiCallLogService aiCallLogService;
    private final ChatClient chatClient;

    /**
     * 构造方法，注入依赖并构建 ChatClient 实例。
     *
     * @param dataCollectAgent 数据采集 Agent，用于获取比赛结构化数据
     * @param aiCallLogService AI 调用日志服务
     * @param chatClientBuilder Spring AI ChatClient 构建器
     */
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
     * @param match        比赛实体，用于获取 matchId 记录日志
     * @param agentType    AI 智能体类型，标识本次调用的业务场景
     * @param systemPrompt 系统提示词，定义 AI 的角色和输出要求
     * @param userPrompt   用户提示词，包含比赛数据等上下文信息
     * @return AiAgentResponse AI 调用结果，包含内容、耗时、成功/失败状态
     */
    public AiAgentResponse callWithPrompt(Match match, AiAgentType agentType,
                                          String systemPrompt, String userPrompt) {
        // 前置校验：API Key 未配置时直接返回失败，避免无效请求
        if (apiKey == null || apiKey.isBlank()) {
            AiAgentResponse resp = AiAgentResponse.failure("API Key 未配置", 0, agentType, match.getMatchId());
            logResponse(resp, systemPrompt + "\n---\n" + userPrompt);
            return resp;
        }

        String fullPrompt = systemPrompt + "\n---\n" + userPrompt; // 拼接完整提示词用于日志记录
        Exception lastException = null;  // 记录最后一次异常
        long totalStart = System.currentTimeMillis(); // 记录总耗时起始时间

        // 重试循环：最多尝试 maxRetries 次
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            long attemptStart = System.currentTimeMillis(); // 记录本次尝试的起始时间
            try {
                // 通过 ChatClient 发起同步 AI 调用（底层为 HTTP POST 请求到 DeepSeek API）
                String content = chatClient.prompt()
                        .system(systemPrompt)   // 设置系统提示词（定义 AI 角色）
                        .user(userPrompt)       // 设置用户提示词（包含比赛数据）
                        .call()                 // 发起同步调用
                        .content();             // 获取 AI 返回的文本内容
                long elapsed = System.currentTimeMillis() - attemptStart;

                // 校验 AI 返回内容是否有效（排除空内容和常见错误响应）
                if (isContentValid(content)) {
                    AiAgentResponse resp = AiAgentResponse.success(content, elapsed, agentType, match.getMatchId());
                    logResponse(resp, fullPrompt);
                    return resp;
                }

                // AI 返回内容无效（空或包含错误关键词）
                AiAgentResponse resp = AiAgentResponse.failure("AI 模型返回内容为空或无效", elapsed, agentType, match.getMatchId());
                logResponse(resp, fullPrompt);
                return resp;

            } catch (Exception e) {
                lastException = e; // 记录异常，供最终错误信息使用
                long elapsed = System.currentTimeMillis() - attemptStart;
                System.err.println("[RemoteModelAgent] 第 " + attempt + " 次调用失败 (" + elapsed + "ms): " + e.getMessage());

                // 如果还有重试机会，等待 1 秒后再试
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000); // 重试间隔 1 秒
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt(); // 恢复中断状态
                    }
                }
            }
        }

        // 所有重试均失败，构建最终错误响应
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
     * @param match      比赛实体，用于获取 matchId 记录日志
     * @param fullPrompt 系统提示词，定义 AI 的角色和输出要求
     * @param userPrompt 用户提示词，包含比赛数据等上下文信息
     * @param agentType  AI 智能体类型，标识本次调用的业务场景
     * @param onChunk    回调函数，每收到一个文本片段时调用，用于 SSE 实时推送
     * @return AiAgentResponse AI 调用结果，包含完整内容、耗时、成功/失败状态
     */
    public AiAgentResponse streamChat(Match match, String fullPrompt, String userPrompt,
                                         AiAgentType agentType, Consumer<String> onChunk) {
        // 前置校验：API Key 未配置时直接返回失败
        if (apiKey == null || apiKey.isBlank()) {
            AiAgentResponse resp = AiAgentResponse.failure("API Key 未配置", 0, agentType, match.getMatchId());
            logResponse(resp, fullPrompt);
            return resp;
        }

        long start = System.currentTimeMillis();      // 记录流式调用起始时间
        StringBuilder collected = new StringBuilder(); // 收集所有流式片段，拼接为完整文本

        try {
            // 通过 ChatClient 发起流式 AI 调用（底层为流式 HTTP 请求到 DeepSeek API）
            Flux<String> flux = chatClient.prompt()
                    .system(fullPrompt)   // 设置系统提示词（定义 AI 角色）
                    .user(userPrompt)     // 设置用户提示词（包含比赛数据）
                    .stream()             // 发起流式调用，返回 Flux 响应式流
                    .content();           // 获取文本片段流

            // 阻塞消费流式响应：每收到一个 chunk 就追加到 collected 并通过回调推送给上层
            flux.doOnNext(chunk -> {
                collected.append(chunk);  // 拼接文本片段
                onChunk.accept(chunk);    // 回调推送（如 SSE emitter.send）
            }).blockLast();              // 阻塞等待所有片段消费完毕

            // 流式调用完成，封装成功响应
            long elapsed = System.currentTimeMillis() - start;
            AiAgentResponse resp = AiAgentResponse.success(collected.toString(), elapsed, agentType, match.getMatchId());
            logResponse(resp, fullPrompt);
            return resp;

        } catch (Exception e) {
            // 流式调用异常，封装失败响应
            long elapsed = System.currentTimeMillis() - start;
            String errorMsg = "流式 AI 调用失败: " + e.getMessage();
            System.err.println("[RemoteModelAgent] streamChat 失败 (" + elapsed + "ms): " + e.getMessage());
            AiAgentResponse resp = AiAgentResponse.failure(errorMsg, elapsed, agentType, match.getMatchId());
            logResponse(resp, fullPrompt);
            return resp;
        }
    }

    /**
     * 通用 AI 调用（不依赖 Match 对象），用于智能推荐等场景。
     * @param systemPrompt 系统提示词，定义 AI 的角色和输出要求
     * @param userPrompt   用户提示词，包含上下文数据
     * @param agentType    AI 智能体类型，标识本次调用的业务场景
     * @return AiAgentResponse AI 调用结果，包含内容、耗时、成功/失败状态
     */
    public AiAgentResponse callGeneric(String systemPrompt, String userPrompt, AiAgentType agentType) {
        // 前置校验：API Key 未配置时直接返回失败
        if (apiKey == null || apiKey.isBlank()) {
            return AiAgentResponse.failure("API Key 未配置", 0, agentType, "N/A");
        }

        long start = System.currentTimeMillis(); // 记录调用起始时间
        Exception lastException = null;          // 记录最后一次异常

        // 重试循环：最多尝试 maxRetries 次
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // 通过 ChatClient 发起同步 AI 调用（底层为 HTTP POST 请求到 DeepSeek API）
                String content = chatClient.prompt()
                        .system(systemPrompt)   // 设置系统提示词
                        .user(userPrompt)       // 设置用户提示词
                        .call()                 // 发起同步调用
                        .content();             // 获取 AI 返回的文本内容
                long elapsed = System.currentTimeMillis() - start;

                // 内容非空即视为有效
                if (content != null && !content.trim().isEmpty()) {
                    AiAgentResponse resp = AiAgentResponse.success(content, elapsed, agentType, "N/A");
                    logResponse(resp, systemPrompt + "\n---\n" + userPrompt);

                    log.info("智能推荐");

                    return resp;
                }
                // AI 返回空内容
                return AiAgentResponse.failure("AI 返回内容为空", elapsed, agentType, "N/A");
            } catch (Exception e) {
                lastException = e; // 记录异常
                // 如果还有重试机会，等待 1 秒后再试
                if (attempt < maxRetries) {
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                }
            }
        }

        // 所有重试均失败，构建最终错误响应
        String errorMsg = "AI 调用失败: " + (lastException != null ? lastException.getMessage() : "未知错误");
        return AiAgentResponse.failure(errorMsg, System.currentTimeMillis() - start, agentType, "N/A");
    }

    /**
     * 记录 AI 调用响应日志。
     *
     * @param response AI 响应对象
     * @param prompt   发送的完整提示词
     */
    private void logResponse(AiAgentResponse response, String prompt) {
        try {
            aiCallLogService.log(response, prompt);
        } catch (Exception e) {
            System.err.println("[RemoteModelAgent] 日志记录失败: " + e.getMessage());
        }
    }

    /**
     * 校验 AI 返回内容是否有效。
     * <p>
     * 排除空内容以及包含常见错误关键词的短文本（可能是 API 错误响应）。
     * </p>
     *
     * @param content AI 返回的文本内容
     * @return 内容有效返回 true
     */
    private boolean isContentValid(String content) {
        if (content == null || content.trim().isEmpty()) return false;
        String[] errorIndicators = {"错误", "error", "Error", "失败", "无法", "API", "api key"};
        for (String indicator : errorIndicators) {
            if (content.contains(indicator) && content.length() < 50) return false;
        }
        return true;
    }

    /**
     * 构建默认的系统提示词，定义 AI 的赛后分析师角色和报告结构。
     *
     * @return 系统提示词字符串
     */
    private String buildDefaultSystemPrompt() {
        return "你是一名专业的体育赛事分析师。请根据提供的比赛数据，生成一份详细的赛后复盘报告。"
                + "报告应包含：1）比赛结果概述 2）关键球员表现分析 3）关键事件回顾 "
                + "4）胜负原因深度分析 5）针对性改进建议。请使用专业但易懂的中文分析。";
    }

    /**
     * 构建用户提示词，包含比赛的详细结构化数据。
     *
     * @param match 比赛实体
     * @return 用户提示词字符串
     */
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
