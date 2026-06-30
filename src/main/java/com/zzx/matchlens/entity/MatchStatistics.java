package com.zzx.matchlens.entity;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MatchStatistics {
    @JsonIgnore
    private int homeScore;
    @JsonIgnore
    private int awayScore;
    private Map<String, Map<String, Integer>> playerStats = new HashMap<>();
    private Map<String, Integer> teamStats = new HashMap<>();
    private int eventCount;
    private String leadingTeam;
    private int scoreDifference;

    public void updateFromMatch(Match match) {
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();

        // 从事件列表计算比分（team.score 未持久化，不可依赖）
        int calculatedHomeScore = 0;
        int calculatedAwayScore = 0;
        for (com.zzx.matchlens.entity.MatchEvent e : match.getEvents()) {
            if (e.getEventType() == com.zzx.matchlens.common.EventType.SCORE && e.getScoreValue() > 0) {
                if (home != null && home.getTeamId() != null && home.getTeamId().equals(e.getTeamId())) {
                    calculatedHomeScore += e.getScoreValue();
                } else if (away != null && away.getTeamId() != null && away.getTeamId().equals(e.getTeamId())) {
                    calculatedAwayScore += e.getScoreValue();
                }
            }
        }

        this.homeScore = calculatedHomeScore;
        this.awayScore = calculatedAwayScore;
        this.scoreDifference = Math.abs(homeScore - awayScore);
        this.leadingTeam = homeScore > awayScore ? (home != null ? home.getTeamName() : null)
                : awayScore > homeScore ? (away != null ? away.getTeamName() : null) : null;
        this.eventCount = match.getEvents().size();

        teamStats.clear();
        if (home != null && home.getPlayers() != null) {
            for (Player p : home.getPlayers()) {
                playerStats.put(p.getPlayerId(), new HashMap<>(p.getStatistics()));
                p.getStatistics().forEach((key, value) ->
                        teamStats.merge(home.getTeamName() + "_" + key, value, Integer::sum));
            }
        }
        if (away != null && away.getPlayers() != null) {
            for (Player p : away.getPlayers()) {
                playerStats.put(p.getPlayerId(), new HashMap<>(p.getStatistics()));
                p.getStatistics().forEach((key, value) ->
                        teamStats.merge(away.getTeamName() + "_" + key, value, Integer::sum));
            }
        }
    }

    @JsonProperty("scoreA")
    public int getHomeScore() { return homeScore; }
    public void setHomeScore(int homeScore) { this.homeScore = homeScore; }
    @JsonProperty("scoreB")
    public int getAwayScore() { return awayScore; }
    public void setAwayScore(int awayScore) { this.awayScore = awayScore; }
    public Map<String, Map<String, Integer>> getPlayerStats() { return playerStats; }
    public void setPlayerStats(Map<String, Map<String, Integer>> playerStats) { this.playerStats = playerStats; }
    public Map<String, Integer> getTeamStats() { return teamStats; }
    public void setTeamStats(Map<String, Integer> teamStats) { this.teamStats = teamStats; }
    public int getEventCount() { return eventCount; }
    public void setEventCount(int eventCount) { this.eventCount = eventCount; }
    public String getLeadingTeam() { return leadingTeam; }
    public void setLeadingTeam(String leadingTeam) { this.leadingTeam = leadingTeam; }
    public int getScoreDifference() { return scoreDifference; }
    public void setScoreDifference(int scoreDifference) { this.scoreDifference = scoreDifference; }
}
