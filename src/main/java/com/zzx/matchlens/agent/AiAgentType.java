package com.zzx.matchlens.agent;

public enum AiAgentType {
    SITUATION_ANALYSIS("实时态势分析"),
    REVIEW_REPORT("赛后智能复盘"),
    MATCH_RECOMMENDATION("智能比赛推荐"),
    MATCH_QA("赛事智能问答");

    private final String description;

    AiAgentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
