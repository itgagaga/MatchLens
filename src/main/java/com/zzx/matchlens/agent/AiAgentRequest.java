package com.zzx.matchlens.agent;

import com.zzx.matchlens.entity.Match;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 智能体请求封装类。
 * <p>
 * 将一次 AI 分析请求所需的所有参数封装为不可变对象，
 * 包括目标比赛、Agent 类型以及可选的自定义系统提示词。
 * </p>
 */
@Getter
@AllArgsConstructor
public class AiAgentRequest {

    /** 待分析的比赛实体 */
    private final Match match;

    /** AI Agent 类型，决定使用哪种分析策略（如复盘、态势分析等） */
    private final AiAgentType agentType;

    /** 自定义系统提示词，用于覆盖默认的 AI 角色设定（可为 null） */
    private final String customSystemPrompt;
}
