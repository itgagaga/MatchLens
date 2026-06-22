package com.zzx.matchlens.dto;

import com.zzx.matchlens.common.EventType;

public class RecordEventRequest {
    private String teamId;
    private String playerId;
    private EventType eventType;
    private int scoreValue;
    private String description;

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public int getScoreValue() { return scoreValue; }
    public void setScoreValue(int scoreValue) { this.scoreValue = scoreValue; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
