package com.zzx.matchlens.entity;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 比赛统计数据实体类，聚合一场比赛的实时统计信息。
 * 包含双方比分、球员统计、球队统计、事件总数、领先方及分差等，
 * 通过重放比赛事件计算得出，不对应独立数据库表，作为 Match 的嵌套对象使用。
 */
@Getter
@Setter
public class MatchStatistics {
    /** 主队得分 */
    @JsonIgnore
    private int homeScore;
    /** 客队得分 */
    @JsonIgnore
    private int awayScore;
    /** 球员统计数据，key为球员ID，value为该球员各项统计的键值对 */
    private Map<String, Map<String, Integer>> playerStats = new HashMap<>();
    /** 球队统计数据，key为"球队名_统计项"，value为统计值 */
    private Map<String, Integer> teamStats = new HashMap<>();
    /** 事件总数 */
    private int eventCount;
    /** 当前领先的球队名称 */
    private String leadingTeam;
    /** 双方分差 */
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

    @JsonProperty("scoreB")
    public int getAwayScore() { return awayScore; }
}
