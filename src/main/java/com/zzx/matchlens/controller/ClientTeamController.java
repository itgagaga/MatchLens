package com.zzx.matchlens.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.MatchEvent;
import com.zzx.matchlens.entity.Player;
import com.zzx.matchlens.entity.Team;
import com.zzx.matchlens.mapper.MatchEventMapper;
import com.zzx.matchlens.mapper.PlayerMapper;
import com.zzx.matchlens.mapper.TeamMapper;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户端球队浏览接口（USER 角色可访问，只读）
 */
@RestController
@RequestMapping("/api/client/teams")
public class ClientTeamController {

    private final TeamMapper teamMapper;                   // 球队 Mapper，提供球队表的数据库访问
    private final PlayerMapper playerMapper;               // 球员 Mapper，提供球员表的数据库访问
    private final MatchEventMapper matchEventMapper;       // 事件 Mapper，用于从事件重算球员统计

    public ClientTeamController(TeamMapper teamMapper, PlayerMapper playerMapper, MatchEventMapper matchEventMapper) {
        this.teamMapper = teamMapper;
        this.playerMapper = playerMapper;
        this.matchEventMapper = matchEventMapper;
    }

    /**
     * 获取所有球队及其球员列表（含从事件重算的统计）
     */
    @GetMapping
    public List<Map<String, Object>> getAllTeamsWithPlayers() {
        List<Team> teams = teamMapper.selectList(null);
        return teams.stream().map(team -> buildTeamMap(team)).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取单个球队详情及其球员（含从事件重算的统计）
     */
    @GetMapping("/{teamId}")
    public Result<Map<String, Object>> getTeamWithPlayers(@PathVariable String teamId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            return Result.fail("球队不存在");
        }
        return Result.ok(buildTeamMap(team));
    }

    /**
     * 构建球队响应数据，从比赛事件重算球员统计和球队总得分
     */
    private Map<String, Object> buildTeamMap(Team team) {
        List<Player> players = playerMapper.selectList(
                new LambdaQueryWrapper<Player>().eq(Player::getTeamId, team.getTeamId()));

        // 从比赛事件重算球员统计
        rebuildPlayerStatsFromEvents(team.getTeamId(), players);

        // 计算球队总得分（从球员 SCORE 统计汇总）
        int totalScore = players.stream()
                .mapToInt(p -> p.getStatistics().getOrDefault("SCORE", 0))
                .sum();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("teamId", team.getTeamId());
        map.put("teamName", team.getTeamName());
        map.put("city", team.getCity());
        map.put("coachName", team.getCoachName());
        map.put("totalScore", totalScore);
        map.put("players", players);
        return map;
    }

    /**
     * 从该球队参与的所有比赛事件中重算每位球员的统计数据
     */
    private void rebuildPlayerStatsFromEvents(String teamId, List<Player> players) {
        // 先清空统计
        players.forEach(p -> p.getStatistics().clear());

        // 构建 playerId -> Player 查找表
        Map<String, Player> playerMap = new LinkedHashMap<>();
        players.forEach(p -> playerMap.put(p.getPlayerId(), p));

        // 查询该球队参与的所有比赛事件
        List<MatchEvent> events = matchEventMapper.selectList(
                new LambdaQueryWrapper<MatchEvent>().eq(MatchEvent::getTeamId, teamId));

        for (MatchEvent event : events) {
            Player player = playerMap.get(event.getPlayerId());
            if (player != null) {
                String statKey = event.getEventType().name();
                int statValue = (event.getEventType() == EventType.SCORE && event.getScoreValue() > 0)
                        ? event.getScoreValue() : 1;
                player.addStat(statKey, statValue);
            }
        }
    }
}
