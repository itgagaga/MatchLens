package com.zzx.matchlens.agent;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.strategy.AnalysisStrategy;
import com.zzx.matchlens.strategy.AnalysisStrategyFactory;
import org.springframework.stereotype.Component;

@Component
public class SituationAnalysisAgent implements AiAgent {

    private final DataCollectAgent dataCollectAgent;

    public SituationAnalysisAgent(DataCollectAgent dataCollectAgent) {
        this.dataCollectAgent = dataCollectAgent;
    }

    @Override
    public String execute(Match match) {
        return analyze(match);
    }

    public String analyze(Match match) {
        AnalysisStrategy strategy = AnalysisStrategyFactory.getStrategy(match.getSportType());
        String strategyResult = strategy.analyze(match);

        return strategyResult;
    }
}
