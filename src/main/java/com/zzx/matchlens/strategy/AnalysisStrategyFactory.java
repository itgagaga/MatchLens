package com.zzx.matchlens.strategy;

import com.zzx.matchlens.common.SportType;

public class AnalysisStrategyFactory {

    public static AnalysisStrategy getStrategy(SportType sportType) {
        return switch (sportType) {
            case BASKETBALL -> new BasketballAnalysisStrategy();
            case FOOTBALL -> new FootballAnalysisStrategy();
            case VOLLEYBALL -> new VolleyballAnalysisStrategy();
            case GENERAL -> new GeneralAnalysisStrategy();
        };
    }
}
