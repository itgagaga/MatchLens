package com.zzx.matchlens.agent;

import com.zzx.matchlens.entity.Match;

/**
 * AI 智能体接口（策略模式）。
 * <p>
 * 定义了所有 AI Agent 的统一执行契约：接收一个 {@link Match} 对象，
 * 返回分析/生成的文本结果。不同的 Agent 实现类负责不同的智能分析场景，
 * 如数据采集、本地规则复盘、远程模型复盘等。
 * </p>
 */
public interface AiAgent {

    /**
     * 执行 AI 分析任务。
     *
     * @param match 待分析的比赛实体，包含队伍、球员、事件等完整数据
     * @return 分析结果的文本描述
     */
    String execute(Match match);
}
