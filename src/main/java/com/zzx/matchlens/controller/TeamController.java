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

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamMapper teamMapper;
    private final PlayerMapper playerMapper;

    public TeamController(TeamMapper teamMapper, PlayerMapper playerMapper) {
        this.teamMapper = teamMapper;
        this.playerMapper = playerMapper;
    }

    @GetMapping
    public List<Team> getAllTeams() {
        return teamMapper.selectList(null);
    }

    @GetMapping("/{teamId}")
    public Team getTeam(@PathVariable String teamId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("球队不存在: " + teamId);
        }
        return team;
    }

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

    @GetMapping("/{teamId}/players")
    public List<Player> getTeamPlayers(@PathVariable String teamId) {
        return playerMapper.selectList(
                new LambdaQueryWrapper<Player>().eq(Player::getTeamId, teamId));
    }
}
