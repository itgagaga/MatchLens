package com.zzx.matchlens.agent;

import com.zzx.matchlens.entity.Match;

public class AiAgentRequest {

    private final Match match;
    private final AiAgentType agentType;
    private final String customSystemPrompt;

    public AiAgentRequest(Match match, AiAgentType agentType, String customSystemPrompt) {
        this.match = match;
        this.agentType = agentType;
        this.customSystemPrompt = customSystemPrompt;
    }

    public Match getMatch() { return match; }
    public AiAgentType getAgentType() { return agentType; }
    public String getCustomSystemPrompt() { return customSystemPrompt; }
}
