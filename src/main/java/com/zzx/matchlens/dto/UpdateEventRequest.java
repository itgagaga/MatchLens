package com.zzx.matchlens.dto;

import com.zzx.matchlens.common.EventType;

import java.time.LocalDateTime;

public class UpdateEventRequest {
    private String teamId;
    private String playerId;
    private EventType eventType;
    private int scoreValue;
    private LocalDateTime eventTime;
    private String description;

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public int getScoreValue() { return scoreValue; }
    public void setScoreValue(int scoreValue) { this.scoreValue = scoreValue; }
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
