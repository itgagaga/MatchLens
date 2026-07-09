package com.zzx.matchlens.strategy;

import com.zzx.matchlens.common.SportType;

/**
 * 分析策略工厂（简单工厂模式）。
 * <p>
 * 根据赛事类型（{@link SportType}）创建对应的 {@link AnalysisStrategy} 实现类实例，
 * 支持篮球、足球、排球和通用赛事四种分析策略。
 * </p>
 */
public class AnalysisStrategyFactory {

    /**
     * 根据赛事类型获取对应的分析策略。
     *
     * @param sportType 赛事类型
     * @return 对应的分析策略实例
     */
    public static AnalysisStrategy getStrategy(SportType sportType) {
        return switch (sportType) {
            case BASKETBALL -> new BasketballAnalysisStrategy();
            case FOOTBALL -> new FootballAnalysisStrategy();
            case VOLLEYBALL -> new VolleyballAnalysisStrategy();
            case GENERAL -> new GeneralAnalysisStrategy();
        };
    }
}
