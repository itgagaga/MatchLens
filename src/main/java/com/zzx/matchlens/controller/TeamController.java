package com.zzx.matchlens.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.Player;
import com.zzx.matchlens.entity.Team;
import com.zzx.matchlens.mapper.PlayerMapper;
import com.zzx.matchlens.mapper.TeamMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 管理端球队管理接口（ADMIN 角色可访问）
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamMapper teamMapper;       // 球队 Mapper，提供球队表的 CRUD 操作
    private final PlayerMapper playerMapper;   // 球员 Mapper，用于查询球队下属球员和删除前校验

    public TeamController(TeamMapper teamMapper, PlayerMapper playerMapper) {
        this.teamMapper = teamMapper;
        this.playerMapper = playerMapper;
    }

    /**
     * 获取所有球队列表
     */
    @GetMapping
    public List<Team> getAllTeams() {
        return teamMapper.selectList(null);
    }

    /**
     * 获取单个球队详情
     */
    @GetMapping("/{teamId}")
    public Team getTeam(@PathVariable String teamId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("球队不存在: " + teamId);
        }
        return team;
    }

    /**
     * 创建新球队
     */
    @PostMapping
    public Result<String> createTeam(@RequestBody Team team) {
        if (team.getTeamName() == null || team.getTeamName().trim().isEmpty()) {
            return Result.fail("球队名称不能为空");
        }
        team.setTeamId(UUID.randomUUID().toString());
        if (team.getScore() == 0) {
            team.setScore(0);
        }
        teamMapper.insert(team);
        return Result.ok("球队创建成功", team.getTeamId());
    }

    /**
     * 更新球队信息
     */
    @PutMapping("/{teamId}")
    public Result<String> updateTeam(@PathVariable String teamId, @RequestBody Team team) {
        Team existing = teamMapper.selectById(teamId);
        if (existing == null) {
            return Result.fail("球队不存在: " + teamId);
        }
        if (team.getTeamName() == null || team.getTeamName().trim().isEmpty()) {
            return Result.fail("球队名称不能为空");
        }
        existing.setTeamName(team.getTeamName());
        existing.setCity(team.getCity());
        existing.setCoachName(team.getCoachName());
        teamMapper.updateById(existing);
        return Result.ok("球队信息更新成功");
    }

    /**
     * 删除球队（球队下无球员时才可删除）
     */
    @DeleteMapping("/{teamId}")
    public Result<String> deleteTeam(@PathVariable String teamId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            return Result.fail("球队不存在: " + teamId);
        }
        Long playerCount = playerMapper.selectCount(
                new LambdaQueryWrapper<Player>().eq(Player::getTeamId, teamId));
        if (playerCount > 0) {
            return Result.fail("该球队下还有球员，不能删除");
        }
        teamMapper.deleteById(teamId);
        return Result.ok("球队删除成功");
    }

    /**
     * 获取指定球队的球员列表
     */
    @GetMapping("/{teamId}/players")
    public List<Player> getTeamPlayers(@PathVariable String teamId) {
        return playerMapper.selectList(
                new LambdaQueryWrapper<Player>().eq(Player::getTeamId, teamId));
    }
}
