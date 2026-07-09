package com.zzx.matchlens.strategy;

import com.zzx.matchlens.entity.Match;

/**
 * 比赛分析策略接口（策略模式）。
 * <p>
 * 定义不同运动类型的态势分析契约，每种运动类型提供各自的实现，
 * 通过 {@link AnalysisStrategyFactory} 根据赛事类型获取对应策略实例。
 * 作为 {@link com.zzx.matchlens.agent.SituationAnalysisAgent} 的本地降级方案。
 * </p>
 */
public interface AnalysisStrategy {

    /**
     * 对比赛进行实时态势分析。
     *
     * @param match 待分析的比赛实体
     * @return 态势分析文本
     */
    String analyze(Match match);
}
