package com.zzx.matchlens.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzx.matchlens.entity.*;
import com.zzx.matchlens.mapper.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MatchRepository {

    private final MatchMapper matchMapper;
    private final TeamMapper teamMapper;
    private final PlayerMapper playerMapper;
    private final MatchEventMapper matchEventMapper;
    private final PlayerStatisticsMapper playerStatisticsMapper;

    public MatchRepository(MatchMapper matchMapper,
                           TeamMapper teamMapper,
                           PlayerMapper playerMapper,
                           MatchEventMapper matchEventMapper,
                           PlayerStatisticsMapper playerStatisticsMapper) {
        this.matchMapper = matchMapper;
        this.teamMapper = teamMapper;
        this.playerMapper = playerMapper;
        this.matchEventMapper = matchEventMapper;
        this.playerStatisticsMapper = playerStatisticsMapper;
    }

    /**
     * 保存比赛及其关联数据（队伍、球员、事件、统计）
     */
    public Match save(Match match) {
        // 1. 保存/更新队伍
        if (match.getHomeTeam() != null) {
            teamMapper.insertOrUpdate(match.getHomeTeam());
            match.setHomeTeamId(match.getHomeTeam().getTeamId());
            savePlayers(match.getHomeTeam());
        }
        if (match.getAwayTeam() != null) {
            teamMapper.insertOrUpdate(match.getAwayTeam());
            match.setAwayTeamId(match.getAwayTeam().getTeamId());
            savePlayers(match.getAwayTeam());
        }

        // 2. 保存/更新比赛
        matchMapper.insertOrUpdate(match);

        // 3. 保存比赛事件（只追加新事件）
        if (match.getEvents() != null) {
            for (MatchEvent event : match.getEvents()) {
                matchEventMapper.insertOrUpdate(event);
            }
        }

        // 4. 保存球员统计
        if (match.getHomeTeam() != null) {
            savePlayerStatistics(match.getHomeTeam(), match.getMatchId());
        }
        if (match.getAwayTeam() != null) {
            savePlayerStatistics(match.getAwayTeam(), match.getMatchId());
        }

        return match;
    }

    /**
     * 根据 ID 查询完整比赛对象（含关联数据）
     */
    public Optional<Match> findById(String matchId) {
        Match match = matchMapper.selectById(matchId);
        if (match == null) {
            return Optional.empty();
        }

        // MyBatis-Plus 通过反射直接设置字段，不会调用 setter，需手动同步状态对象
        match.syncState();

        // 加载甲方
        if (match.getHomeTeamId() != null) {
            Team homeTeam = teamMapper.selectById(match.getHomeTeamId());
            if (homeTeam != null) {
                homeTeam.setPlayers(loadPlayers(homeTeam.getTeamId()));
                match.setHomeTeam(homeTeam);
            }
        }

        // 加载乙方
        if (match.getAwayTeamId() != null) {
            Team awayTeam = teamMapper.selectById(match.getAwayTeamId());
            if (awayTeam != null) {
                awayTeam.setPlayers(loadPlayers(awayTeam.getTeamId()));
                match.setAwayTeam(awayTeam);
            }
        }

        // 加载比赛事件
        List<MatchEvent> events = matchEventMapper.selectList(
                new LambdaQueryWrapper<MatchEvent>()
                        .eq(MatchEvent::getMatchId, matchId)
                        .orderByAsc(MatchEvent::getEventTime));
        match.setEvents(events);

        // 加载球员统计并填充到 Player 对象
        loadPlayerStatistics(match);

        return Optional.of(match);
    }

    /**
     * 查询所有比赛（含队伍信息、事件和球员列表）
     */
    public List<Match> findAll() {
        List<Match> matches = matchMapper.selectList(null);
        for (Match match : matches) {
            match.syncState();
            if (match.getHomeTeamId() != null) {
                Team homeTeam = teamMapper.selectById(match.getHomeTeamId());
                if (homeTeam != null) {
                    homeTeam.setPlayers(loadPlayers(homeTeam.getTeamId()));
                }
                match.setHomeTeam(homeTeam);
            }
            if (match.getAwayTeamId() != null) {
                Team awayTeam = teamMapper.selectById(match.getAwayTeamId());
                if (awayTeam != null) {
                    awayTeam.setPlayers(loadPlayers(awayTeam.getTeamId()));
                }
                match.setAwayTeam(awayTeam);
            }
            // 加载事件列表，以便计算比分统计
            List<MatchEvent> events = matchEventMapper.selectList(
                    new LambdaQueryWrapper<MatchEvent>()
                            .eq(MatchEvent::getMatchId, match.getMatchId())
                            .orderByAsc(MatchEvent::getEventTime));
            match.setEvents(events);
        }
        return matches;
    }

    /**
     * 删除比赛及其关联数据
     */
    public void delete(String matchId) {
        matchEventMapper.delete(
                new LambdaQueryWrapper<MatchEvent>().eq(MatchEvent::getMatchId, matchId));
        playerStatisticsMapper.delete(
                new LambdaQueryWrapper<PlayerStatistics>().eq(PlayerStatistics::getMatchId, matchId));
        matchMapper.deleteById(matchId);
    }

    // ========== 私有辅助方法 ==========

    private void savePlayers(Team team) {
        if (team.getPlayers() == null) return;
        for (Player player : team.getPlayers()) {
            player.setTeamId(team.getTeamId());
            playerMapper.insertOrUpdate(player);
        }
    }

    private List<Player> loadPlayers(String teamId) {
        return playerMapper.selectList(
                new LambdaQueryWrapper<Player>().eq(Player::getTeamId, teamId));
    }

    private void savePlayerStatistics(Team team, String matchId) {
        if (team.getPlayers() == null) return;
        for (Player player : team.getPlayers()) {
            if (player.getStatistics() == null) continue;
            player.getStatistics().forEach((statKey, statValue) -> {
                // 先删除旧记录再插入（upsert）
                playerStatisticsMapper.delete(
                        new LambdaQueryWrapper<PlayerStatistics>()
                                .eq(PlayerStatistics::getPlayerId, player.getPlayerId())
                                .eq(PlayerStatistics::getMatchId, matchId)
                                .eq(PlayerStatistics::getStatKey, statKey));
                playerStatisticsMapper.insert(
                        new PlayerStatistics(player.getPlayerId(), matchId, statKey, statValue));
            });
        }
    }

    private void loadPlayerStatistics(Match match) {
        List<PlayerStatistics> statsList = playerStatisticsMapper.selectList(
                new LambdaQueryWrapper<PlayerStatistics>()
                        .eq(PlayerStatistics::getMatchId, match.getMatchId()));

        for (PlayerStatistics ps : statsList) {
            Player player = findPlayerInMatch(match, ps.getPlayerId());
            if (player != null) {
                player.addStat(ps.getStatKey(), ps.getStatValue());
            }
        }
    }

    private Player findPlayerInMatch(Match match, String playerId) {
        if (match.getHomeTeam() != null) {
            Player p = match.getHomeTeam().findPlayer(playerId);
            if (p != null) return p;
        }
        if (match.getAwayTeam() != null) {
            return match.getAwayTeam().findPlayer(playerId);
        }
        return null;
    }
}
