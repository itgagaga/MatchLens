package com.zzx.matchlens.controller;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.dto.CreatePlayerRequest;
import com.zzx.matchlens.dto.UpdatePlayerRequest;
import com.zzx.matchlens.entity.MatchEvent;
import com.zzx.matchlens.entity.Player;
import com.zzx.matchlens.entity.PlayerStatistics;
import com.zzx.matchlens.service.PlayerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端球员管理接口（ADMIN 角色可访问）
 */
@RestController
@RequestMapping("/api/players")
public class PlayerController {

    /** 球员服务，处理球员 CRUD、事件查询和统计查询等业务 */
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * 查询球员列表（可按球队筛选）
     */
    @GetMapping
    public List<Player> queryPlayers(@RequestParam(required = false) String teamId) {
        if (teamId != null && !teamId.isEmpty()) {
            return playerService.getPlayersByTeam(teamId);
        }
        return playerService.getAllPlayers();
    }

    /**
     * 获取单个球员详情
     */
    @GetMapping("/{playerId}")
    public Player getPlayer(@PathVariable String playerId) {
        return playerService.getPlayer(playerId);
    }

    /**
     * 创建新球员
     */
    @PostMapping
    public Result<String> createPlayer(@RequestBody CreatePlayerRequest request) {
        return playerService.createPlayer(
            request.getPlayerName(), request.getTeamId(), request.getNumber(),
            request.getPosition(), request.getAge(), request.getHeight(), request.getWeight());
    }

    /**
     * 更新球员信息
     */
    @PutMapping("/{playerId}")
    public Result<String> updatePlayer(@PathVariable String playerId,
                                       @RequestBody UpdatePlayerRequest request) {
        return playerService.updatePlayer(
            playerId, request.getPlayerName(), request.getNumber(),
            request.getPosition(), request.getAge(), request.getHeight(), request.getWeight());
    }

    /**
     * 删除球员
     */
    @DeleteMapping("/{playerId}")
    public Result<String> deletePlayer(@PathVariable String playerId) {
        return playerService.deletePlayer(playerId);
    }

    /**
     * 查询球员关联的比赛事件
     */
    @GetMapping("/{playerId}/events")
    public List<MatchEvent> getPlayerEvents(@PathVariable String playerId) {
        return playerService.getPlayerEvents(playerId);
    }

    /**
     * 查询球员的统计数据
     */
    @GetMapping("/{playerId}/statistics")
    public List<PlayerStatistics> getPlayerStatistics(@PathVariable String playerId) {
        return playerService.getPlayerStatistics(playerId);
    }
}
