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

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public List<Player> queryPlayers(@RequestParam(required = false) String teamId) {
        if (teamId != null && !teamId.isEmpty()) {
            return playerService.getPlayersByTeam(teamId);
        }
        return playerService.getAllPlayers();
    }

    @GetMapping("/{playerId}")
    public Player getPlayer(@PathVariable String playerId) {
        return playerService.getPlayer(playerId);
    }

    @PostMapping
    public Result<String> createPlayer(@RequestBody CreatePlayerRequest request) {
        return playerService.createPlayer(
            request.getPlayerName(), request.getTeamId(), request.getNumber(),
            request.getPosition(), request.getAge(), request.getHeight(), request.getWeight());
    }

    @PutMapping("/{playerId}")
    public Result<String> updatePlayer(@PathVariable String playerId,
                                       @RequestBody UpdatePlayerRequest request) {
        return playerService.updatePlayer(
            playerId, request.getPlayerName(), request.getNumber(),
            request.getPosition(), request.getAge(), request.getHeight(), request.getWeight());
    }

    @DeleteMapping("/{playerId}")
    public Result<String> deletePlayer(@PathVariable String playerId) {
        return playerService.deletePlayer(playerId);
    }

    @GetMapping("/{playerId}/events")
    public List<MatchEvent> getPlayerEvents(@PathVariable String playerId) {
        return playerService.getPlayerEvents(playerId);
    }

    @GetMapping("/{playerId}/statistics")
    public List<PlayerStatistics> getPlayerStatistics(@PathVariable String playerId) {
        return playerService.getPlayerStatistics(playerId);
    }
}
