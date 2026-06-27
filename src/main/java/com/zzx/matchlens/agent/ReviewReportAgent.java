package com.zzx.matchlens.agent;

import com.zzx.matchlens.entity.Match;
import org.springframework.stereotype.Component;

@Component
public class ReviewReportAgent implements AiAgent {

    private final DataCollectAgent dataCollectAgent;
    private final RemoteModelAgent remoteModelAgent;
    private final LocalRuleAgent localRuleAgent;

    public ReviewReportAgent(DataCollectAgent dataCollectAgent,
                             RemoteModelAgent remoteModelAgent,
                             LocalRuleAgent localRuleAgent) {
        this.dataCollectAgent = dataCollectAgent;
        this.remoteModelAgent = remoteModelAgent;
        this.localRuleAgent = localRuleAgent;
    }

    @Override
    public String execute(Match match) {
        return generateReview(match);
    }

    public String generateReview(Match match) {
        // 优先调用 DeepSeek API
        String remoteResult = remoteModelAgent.execute(match);

        // API 调用失败时降级到本地规则
        if (remoteResult == null || remoteResult.isEmpty()) {
            System.out.println("[ReviewReportAgent] 远程模型调用失败，降级到本地规则模板");
            return localRuleAgent.execute(match);
        }

        return remoteResult;
    }
}
