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
        String systemPrompt = buildSystemPrompt(match);
        String userPrompt = dataCollectAgent.collect(match);

        AiAgentResponse remoteResponse = remoteModelAgent.callWithPrompt(
                match, AiAgentType.REVIEW_REPORT, systemPrompt, userPrompt);

        if (remoteResponse.isSuccess()) {
            return remoteResponse.getContent();
        }

        System.out.println("[ReviewReportAgent] 远程 AI 调用失败: "
                + remoteResponse.getErrorMessage() + "，降级到本地规则模板");
        return localRuleAgent.execute(match);
    }

    private String buildSystemPrompt(Match match) {
        return "你是一名专业的体育赛事分析师。请根据提供的比赛数据，生成一份详细的赛后复盘报告。\n\n"
                + "报告应包含：\n"
                + "1）比赛结果概述\n"
                + "2）关键球员表现分析\n"
                + "3）关键事件回顾\n"
                + "4）胜负原因深度分析\n"
                + "5）针对性改进建议\n\n"
                + "【赛事信息】\n"
                + "赛事类型：" + match.getSportType() + "\n"
                + "比赛状态：" + match.getStatus() + "\n"
                + "甲方：" + (match.getHomeTeam() != null ? match.getHomeTeam().getTeamName() : "未知") + "\n"
                + "乙方：" + (match.getAwayTeam() != null ? match.getAwayTeam().getTeamName() : "未知") + "\n\n"
                + "请使用专业但易懂的中文分析。";
    }
}
