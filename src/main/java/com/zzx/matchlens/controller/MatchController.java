package com.zzx.matchlens.controller;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.dto.AddPlayerRequest;
import com.zzx.matchlens.dto.CreateMatchRequest;
import com.zzx.matchlens.dto.SetTeamsRequest;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.service.MatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping
    public Match createMatch(@RequestBody CreateMatchRequest request) {
        return matchService.createMatch(request.getMatchName(), request.getSportType());
    }

    @GetMapping
    public List<Match> getAllMatches() {
        return matchService.getAllMatches();
    }

    @GetMapping("/{id}")
    public Match getMatch(@PathVariable String id) {
        Match match = matchService.getMatch(id);
        // 重算统计信息（statistics 未持久化，每次加载需从事件重新计算）
        if (match.getHomeTeam() != null && match.getAwayTeam() != null) {
            match.getStatistics().updateFromMatch(match);
        }
        return match;
    }

    @PostMapping("/{id}/teams")
    public Result<String> setTeams(@PathVariable String id, @RequestBody SetTeamsRequest request) {
        return matchService.setTeams(id, request.getTeamAName(), request.getTeamBName());
    }

    @PostMapping("/{id}/players")
    public Result<String> addPlayer(@PathVariable String id, @RequestBody AddPlayerRequest request) {
        return matchService.addPlayer(id, request.isTeamA(), request.getPlayerName(), request.getNumber());
    }

    @PostMapping("/{id}/start")
    public Result<String> startMatch(@PathVariable String id) {
        return matchService.startMatch(id);
    }

    @PostMapping("/{id}/pause")
    public Result<String> pauseMatch(@PathVariable String id) {
        return matchService.pauseMatch(id);
    }

    @PostMapping("/{id}/resume")
    public Result<String> resumeMatch(@PathVariable String id) {
        return matchService.resumeMatch(id);
    }

    @PostMapping("/{id}/finish")
    public Result<String> finishMatch(@PathVariable String id) {
        return matchService.finishMatch(id);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteMatch(@PathVariable String id) {
        return matchService.deleteMatch(id);
    }

    @PutMapping("/{id}")
    public Result<String> updateMatchName(@PathVariable String id, @RequestBody CreateMatchRequest request) {
        return matchService.updateMatchName(id, request.getMatchName());
    }
}
