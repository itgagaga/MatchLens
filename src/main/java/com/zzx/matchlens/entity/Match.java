package com.zzx.matchlens.entity;

import com.zzx.matchlens.common.MatchStatus;
import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.common.SportType;
import com.zzx.matchlens.state.*;

import java.util.ArrayList;
import java.util.List;

public class Match {
    private String matchId;
    private String matchName;
    private SportType sportType;
    private Team homeTeam;
    private Team awayTeam;
    private MatchStatus status = MatchStatus.NOT_STARTED;
    private transient MatchState state = new NotStartedState();
    private List<MatchEvent> events = new ArrayList<>();
    private MatchStatistics statistics = new MatchStatistics();

    public Match() {}

    public Match(String matchId, String matchName, SportType sportType) {
        this.matchId = matchId;
        this.matchName = matchName;
        this.sportType = sportType;
    }

    public void addEvent(MatchEvent event) {
        events.add(event);
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

    private void syncState() {
        this.state = switch (status) {
            case NOT_STARTED -> new NotStartedState();
            case RUNNING -> new RunningState();
            case PAUSED -> new PausedState();
            case FINISHED -> new FinishedState();
        };
    }

    public MatchState getState() { return state; }
    public void setState(MatchState state) { this.state = state; }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
    public String getMatchName() { return matchName; }
    public void setMatchName(String matchName) { this.matchName = matchName; }
    public SportType getSportType() { return sportType; }
    public void setSportType(SportType sportType) { this.sportType = sportType; }
    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }
    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) { this.awayTeam = awayTeam; }
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
