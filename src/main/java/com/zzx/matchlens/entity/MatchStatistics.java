package com.zzx.matchlens.entity;

import java.util.HashMap;
import java.util.Map;

public class MatchStatistics {
    private int homeScore;
    private int awayScore;
    private Map<String, Map<String, Integer>> playerStats = new HashMap<>();
    private Map<String, Integer> teamStats = new HashMap<>();
    private int eventCount;
    private String leadingTeam;
    private int scoreDifference;

    public void updateFromMatch(Match match) {
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();
        this.homeScore = home.getScore();
        this.awayScore = away.getScore();
        this.scoreDifference = Math.abs(homeScore - awayScore);
        this.leadingTeam = homeScore > awayScore ? home.getTeamName()
                : awayScore > homeScore ? away.getTeamName() : null;
        this.eventCount = match.getEvents().size();

        for (Player p : home.getPlayers()) {
            playerStats.put(p.getPlayerId(), new HashMap<>(p.getStatistics()));
        }
        for (Player p : away.getPlayers()) {
            playerStats.put(p.getPlayerId(), new HashMap<>(p.getStatistics()));
        }
    }

    public int getHomeScore() { return homeScore; }
    public void setHomeScore(int homeScore) { this.homeScore = homeScore; }
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
