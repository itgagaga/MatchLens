package com.zzx.matchlens.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zzx.matchlens.common.MatchStatus;
import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.common.SportType;
import com.zzx.matchlens.state.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@TableName("t_match")
public class Match {

    @TableId
    private String matchId;
    private String matchName;
    private SportType sportType;
    private MatchStatus status;
    private String homeTeamId;
    private String awayTeamId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private transient MatchState state = new NotStartedState();

    @TableField(exist = false)
    @JsonIgnore
    private Team homeTeam;

    @TableField(exist = false)
    @JsonIgnore
    private Team awayTeam;

    @TableField(exist = false)
    private List<MatchEvent> events = new ArrayList<>();

    @TableField(exist = false)
    private MatchStatistics statistics = new MatchStatistics();

    public Match() {}

    public Match(String matchId, String matchName, SportType sportType) {
        this.matchId = matchId;
        this.matchName = matchName;
        this.sportType = sportType;
        this.status = MatchStatus.NOT_STARTED;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public void addEvent(MatchEvent event) {
        events.add(event);
    }

    public boolean removeEvent(String eventId) {
        return events.removeIf(e -> e.getEventId().equals(eventId));
    }

    public void clearPlayerStatistics() {
        if (homeTeam != null && homeTeam.getPlayers() != null) {
            homeTeam.getPlayers().forEach(p -> p.getStatistics().clear());
        }
        if (awayTeam != null && awayTeam.getPlayers() != null) {
            awayTeam.getPlayers().forEach(p -> p.getStatistics().clear());
        }
    }

    public Result<String> start() {
        return state.start(this);
    }

    public Result<String> pause() {
        return state.pause(this);
    }

    public Result<String> resume() {
        return state.resume(this);
    }

    public Result<String> finish() {
        return state.finish(this);
    }

    public Result<String> canAddEvent() {
        return state.addEvent(this);
    }

    public Result<String> canModifyTeams() {
        return state.modifyTeams(this);
    }

    public Result<String> canViewReport() {
        return state.viewReport(this);
    }

    public void syncState() {
        if (this.status == null) {
            this.state = new NotStartedState();
            return;
        }
        this.state = switch (status) {
            case NOT_STARTED -> new NotStartedState();
            case RUNNING -> new RunningState();
            case PAUSED -> new PausedState();
            case FINISHED -> new FinishedState();
        };
    }

    @JsonIgnore
    public MatchState getState() { return state; }
    public void setState(MatchState state) { this.state = state; }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
    public String getMatchName() { return matchName; }
    public void setMatchName(String matchName) { this.matchName = matchName; }
    public SportType getSportType() { return sportType; }
    public void setSportType(SportType sportType) { this.sportType = sportType; }
    public String getHomeTeamId() { return homeTeamId; }
    public void setHomeTeamId(String homeTeamId) { this.homeTeamId = homeTeamId; }
    public String getAwayTeamId() { return awayTeamId; }
    public void setAwayTeamId(String awayTeamId) { this.awayTeamId = awayTeamId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    @JsonProperty("teamA")
    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
        if (homeTeam != null) this.homeTeamId = homeTeam.getTeamId();
    }
    @JsonProperty("teamB")
    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
        if (awayTeam != null) this.awayTeamId = awayTeam.getTeamId();
    }

    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) {
        this.status = status;
        syncState();
    }
    public List<MatchEvent> getEvents() { return events; }
    public void setEvents(List<MatchEvent> events) { this.events = events; }
    public MatchStatistics getStatistics() { return statistics; }
    public void setStatistics(MatchStatistics statistics) { this.statistics = statistics; }
}
