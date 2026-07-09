package com.zzx.matchlens.agent;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 智能体响应封装类。
 * <p>
 * 封装一次 AI Agent 调用的完整响应信息，包括是否成功、返回内容、
 * 错误消息、响应耗时、Agent 类型及关联的比赛 ID。
 * 通过静态工厂方法 {@link #success} 和 {@link #failure} 创建实例。
 * </p>
 */
@Getter
@NoArgsConstructor
public class AiAgentResponse {

    /** 调用是否成功 */
    private boolean success;

    /** AI 生成的内容（成功时有值） */
    private String content;

    /** 错误消息（失败时有值） */
    private String errorMessage;

    /** 响应耗时（毫秒） */
    private long responseTimeMs;

    /** 执行此次调用的 Agent 类型 */
    private AiAgentType agentType;

    /** 关联的比赛 ID */
    private String matchId;

    /**
     * 创建成功响应。
     *
     * @param content        AI 生成的内容
     * @param responseTimeMs 响应耗时（毫秒）
     * @param agentType      Agent 类型
     * @param matchId        关联的比赛 ID
     * @return 成功响应实例
     */
    public static AiAgentResponse success(String content, long responseTimeMs, AiAgentType agentType, String matchId) {
        AiAgentResponse r = new AiAgentResponse();
        r.success = true;
        r.content = content;
        r.responseTimeMs = responseTimeMs;
        r.agentType = agentType;
        r.matchId = matchId;
        return r;
    }

    /**
     * 创建失败响应。
     *
     * @param errorMessage   错误描述信息
     * @param responseTimeMs 响应耗时（毫秒）
     * @param agentType      Agent 类型
     * @param matchId        关联的比赛 ID
     * @return 失败响应实例
     */
    public static AiAgentResponse failure(String errorMessage, long responseTimeMs, AiAgentType agentType, String matchId) {
        AiAgentResponse r = new AiAgentResponse();
        r.success = false;
        r.errorMessage = errorMessage;
        r.responseTimeMs = responseTimeMs;
        r.agentType = agentType;
        r.matchId = matchId;
        return r;
    }
}
