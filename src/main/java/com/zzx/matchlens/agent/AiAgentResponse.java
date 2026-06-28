package com.zzx.matchlens.agent;

public class AiAgentResponse {

    private boolean success;
    private String content;
    private String errorMessage;
    private long responseTimeMs;
    private AiAgentType agentType;
    private String matchId;

    public static AiAgentResponse success(String content, long responseTimeMs, AiAgentType agentType, String matchId) {
        AiAgentResponse r = new AiAgentResponse();
        r.success = true;
        r.content = content;
        r.responseTimeMs = responseTimeMs;
        r.agentType = agentType;
        r.matchId = matchId;
        return r;
    }

    public static AiAgentResponse failure(String errorMessage, long responseTimeMs, AiAgentType agentType, String matchId) {
        AiAgentResponse r = new AiAgentResponse();
        r.success = false;
        r.errorMessage = errorMessage;
        r.responseTimeMs = responseTimeMs;
        r.agentType = agentType;
        r.matchId = matchId;
        return r;
    }

    public boolean isSuccess() { return success; }
    public String getContent() { return content; }
    public String getErrorMessage() { return errorMessage; }
    public long getResponseTimeMs() { return responseTimeMs; }
    public AiAgentType getAgentType() { return agentType; }
    public String getMatchId() { return matchId; }
}
