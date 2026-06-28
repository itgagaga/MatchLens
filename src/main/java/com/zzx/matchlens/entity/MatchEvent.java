package com.zzx.matchlens.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zzx.matchlens.common.EventType;

import java.time.LocalDateTime;

@TableName("t_match_event")
public class MatchEvent {

    @TableId
    private String eventId;
    private String matchId;
    private String teamId;
    private String playerId;
    private EventType eventType;
    private int scoreValue;
    private LocalDateTime eventTime;
    private String description;

    public MatchEvent() {}

    public MatchEvent(String eventId, String matchId, String teamId, String playerId,
                      EventType eventType, int scoreValue, String description) {
        this.eventId = eventId;
        this.matchId = matchId;
        this.teamId = teamId;
        this.playerId = playerId;
        this.eventType = eventType;
        this.scoreValue = scoreValue;
        this.description = description;
        this.eventTime = LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
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
