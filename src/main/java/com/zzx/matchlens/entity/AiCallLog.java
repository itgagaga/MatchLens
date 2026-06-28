package com.zzx.matchlens.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("t_ai_call_log")
public class AiCallLog {

    @TableId
    private String id;
    private String agentType;
    private String matchId;
    private String prompt;
    private String response;
    private Integer success;
    private String errorMessage;
    private Long responseTimeMs;
    private LocalDateTime callTime;

    public AiCallLog() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAgentType() { return agentType; }
    public void setAgentType(String agentType) { this.agentType = agentType; }
    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public Integer getSuccess() { return success; }
    public void setSuccess(Integer success) { this.success = success; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public LocalDateTime getCallTime() { return callTime; }
    public void setCallTime(LocalDateTime callTime) { this.callTime = callTime; }
}
