package com.zzx.matchlens.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.MatchEvent;
import com.zzx.matchlens.entity.Player;
import com.zzx.matchlens.entity.PlayerStatistics;
import com.zzx.matchlens.entity.Team;
import com.zzx.matchlens.mapper.MatchEventMapper;
import com.zzx.matchlens.mapper.PlayerMapper;
import com.zzx.matchlens.mapper.PlayerStatisticsMapper;
import com.zzx.matchlens.mapper.TeamMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PlayerService {

    private final PlayerMapper playerMapper;
    private final TeamMapper teamMapper;
    private final MatchEventMapper matchEventMapper;
    private final PlayerStatisticsMapper playerStatisticsMapper;

    public PlayerService(PlayerMapper playerMapper,
                         TeamMapper teamMapper,
                         MatchEventMapper matchEventMapper,
                         PlayerStatisticsMapper playerStatisticsMapper) {
        this.playerMapper = playerMapper;
        this.teamMapper = teamMapper;
        this.matchEventMapper = matchEventMapper;
        this.playerStatisticsMapper = playerStatisticsMapper;
    }

    public List<Player> getAllPlayers() {
        return playerMapper.selectList(null);
    }

    public List<Player> getPlayersByTeam(String teamId) {
        return playerMapper.selectList(
            new LambdaQueryWrapper<Player>().eq(Player::getTeamId, teamId));
    }

    public Player getPlayer(String playerId) {
        Player player = playerMapper.selectById(playerId);
        if (player == null) {
            throw new RuntimeException("球员不存在: " + playerId);
        }
        return player;
    }

    public Result<String> createPlayer(String playerName, String teamId,
                                       int number, String position,
                                       int age, int height, int weight) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return Result.fail("球员姓名不能为空");
        }

        if (number <= 0) {
            return Result.fail("球衣号码必须大于0");
        }

        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            return Result.fail("队伍不存在: " + teamId);
        }

        Long count = playerMapper.selectCount(
            new LambdaQueryWrapper<Player>()
                .eq(Player::getTeamId, teamId)
                .eq(Player::getNumber, number));
        if (count > 0) {
            return Result.fail("该队伍中已存在 " + number + " 号球员");
        }

        Player player = new Player(UUID.randomUUID().toString(), playerName, teamId, number);
        player.setPosition(position);
        player.setAge(age);
        player.setHeight(height);
        player.setWeight(weight);
        playerMapper.insert(player);

        return Result.ok("球员 " + playerName + " 添加成功");
    }

    public Result<String> updatePlayer(String playerId, String playerName,
                                       int number, String position,
                                       int age, int height, int weight) {
        Player player = playerMapper.selectById(playerId);
        if (player == null) {
            return Result.fail("球员不存在: " + playerId);
        }

        if (playerName == null || playerName.trim().isEmpty()) {
            return Result.fail("球员姓名不能为空");
        }

        if (number <= 0) {
            return Result.fail("球衣号码必须大于0");
        }

        if (number != player.getNumber()) {
            Long count = playerMapper.selectCount(
                new LambdaQueryWrapper<Player>()
                    .eq(Player::getTeamId, player.getTeamId())
                    .eq(Player::getNumber, number)
                    .ne(Player::getPlayerId, playerId));
            if (count > 0) {
                return Result.fail("该队伍中已存在 " + number + " 号球员");
            }
        }

        player.setPlayerName(playerName);
        player.setNumber(number);
        player.setPosition(position);
        player.setAge(age);
        player.setHeight(height);
        player.setWeight(weight);
        playerMapper.updateById(player);

        return Result.ok("球员信息更新成功");
    }

    public Result<String> deletePlayer(String playerId) {
        Player player = playerMapper.selectById(playerId);
        if (player == null) {
            return Result.fail("球员不存在: " + playerId);
        }

        Long eventCount = matchEventMapper.selectCount(
            new LambdaQueryWrapper<MatchEvent>().eq(MatchEvent::getPlayerId, playerId));
        if (eventCount > 0) {
            return Result.fail("该球员已有比赛数据，不能删除");
        }

        Long statsCount = playerStatisticsMapper.selectCount(
            new LambdaQueryWrapper<PlayerStatistics>().eq(PlayerStatistics::getPlayerId, playerId));
        if (statsCount > 0) {
            return Result.fail("该球员已有统计数据，不能删除");
        }

        playerMapper.deleteById(playerId);
        return Result.ok("球员删除成功");
    }

    public List<MatchEvent> getPlayerEvents(String playerId) {
        getPlayer(playerId);
        return matchEventMapper.selectList(
            new LambdaQueryWrapper<MatchEvent>()
                .eq(MatchEvent::getPlayerId, playerId)
                .orderByAsc(MatchEvent::getEventTime));
    }

    public List<PlayerStatistics> getPlayerStatistics(String playerId) {
        getPlayer(playerId);
        return playerStatisticsMapper.selectList(
            new LambdaQueryWrapper<PlayerStatistics>()
                .eq(PlayerStatistics::getPlayerId, playerId));
    }
}
