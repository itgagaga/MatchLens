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

        // ===== 1. 从事件重放重建球员统计（不依赖 t_player_statistics，保证一致性）=====
        // 清空所有球员统计
        if (home != null && home.getPlayers() != null) {
            home.getPlayers().forEach(p -> p.getStatistics().clear());
        }
        if (away != null && away.getPlayers() != null) {
            away.getPlayers().forEach(p -> p.getStatistics().clear());
        }

        // 重放事件，重建球员统计和球队比分
        int calculatedHomeScore = 0;
        int calculatedAwayScore = 0;
        for (com.zzx.matchlens.entity.MatchEvent e : match.getEvents()) {
            // 球队比分
            if (e.getEventType() == com.zzx.matchlens.common.EventType.SCORE && e.getScoreValue() > 0) {
                if (home != null && home.getTeamId() != null && home.getTeamId().equals(e.getTeamId())) {
                    calculatedHomeScore += e.getScoreValue();
                } else if (away != null && away.getTeamId() != null && away.getTeamId().equals(e.getTeamId())) {
                    calculatedAwayScore += e.getScoreValue();
                }
            }
            // 球员统计
            Player player = findPlayerInMatch(match, e.getPlayerId());
            if (player != null) {
                String statKey = e.getEventType().name();
                int statValue = (e.getEventType() == com.zzx.matchlens.common.EventType.SCORE && e.getScoreValue() > 0)
                        ? e.getScoreValue() : 1;
                player.addStat(statKey, statValue);
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

    private Player findPlayerInMatch(Match match, String playerId) {
        if (playerId == null) return null;
        if (match.getHomeTeam() != null) {
            Player p = match.getHomeTeam().findPlayer(playerId);
            if (p != null) return p;
        }
        if (match.getAwayTeam() != null) {
            return match.getAwayTeam().findPlayer(playerId);
        }
        return null;
    }

    /**
     * 仅计算聚合统计（比分、分差、领先方、事件数、球队统计），
     * 不清空/重建球员统计。适用于 StatisticsRebuildService 已重放事件后的场景。
     */
    public void computeAggregates(Match match) {
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();

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
