package com.zzx.matchlens.controller;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.dto.AddPlayerRequest;
import com.zzx.matchlens.dto.CreateMatchRequest;
import com.zzx.matchlens.dto.SetTeamsRequest;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.service.MatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端比赛管理接口（ADMIN 角色可访问）
 */
@RestController
@RequestMapping("/api/matches")
public class MatchController {

    /** 比赛服务，处理比赛创建、状态管理、队伍设置、球员添加等业务 */
    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * 创建新比赛
     */
    @PostMapping
    public Match createMatch(@RequestBody CreateMatchRequest request) {
        return matchService.createMatch(request.getMatchName(), request.getSportType());
    }

    /**
     * 获取所有比赛列表（含实时统计）
     */
    @GetMapping
    public List<Match> getAllMatches() {
        List<Match> matches = matchService.getAllMatches();
        // 重算统计信息（statistics 未持久化，每次加载需从事件重新计算）
        for (Match match : matches) {
            if (match.getHomeTeam() != null && match.getAwayTeam() != null) {
                match.getStatistics().updateFromMatch(match);
            }
        }
        return matches;
    }

    /**
     * 获取单场比赛详情（含实时统计）
     */
    @GetMapping("/{id}")
    public Match getMatch(@PathVariable String id) {
        Match match = matchService.getMatch(id);
        // 重算统计信息（statistics 未持久化，每次加载需从事件重新计算）
        if (match.getHomeTeam() != null && match.getAwayTeam() != null) {
            match.getStatistics().updateFromMatch(match);
        }
        return match;
    }

    /**
     * 设置比赛双方队伍
     */
    @PostMapping("/{id}/teams")
    public Result<String> setTeams(@PathVariable String id, @RequestBody SetTeamsRequest request) {
        return matchService.setTeams(id, request.getTeamAName(), request.getTeamBName());
    }

    /**
     * 向比赛添加球员
     */
    @PostMapping("/{id}/players")
    public Result<String> addPlayer(@PathVariable String id, @RequestBody AddPlayerRequest request) {
        return matchService.addPlayer(id, request.isTeamA(), request.getPlayerName(), request.getNumber());
    }

    /**
     * 开始比赛
     */
    @PostMapping("/{id}/start")
    public Result<String> startMatch(@PathVariable String id) {
        return matchService.startMatch(id);
    }

    /**
     * 暂停比赛
     */
    @PostMapping("/{id}/pause")
    public Result<String> pauseMatch(@PathVariable String id) {
        return matchService.pauseMatch(id);
    }

    /**
     * 恢复比赛
     */
    @PostMapping("/{id}/resume")
    public Result<String> resumeMatch(@PathVariable String id) {
        return matchService.resumeMatch(id);
    }

    /**
     * 结束比赛
     */
    @PostMapping("/{id}/finish")
    public Result<String> finishMatch(@PathVariable String id) {
        return matchService.finishMatch(id);
    }

    /**
     * 删除比赛
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteMatch(@PathVariable String id) {
        return matchService.deleteMatch(id);
    }

    /**
     * 修改比赛名称
     */
    @PutMapping("/{id}")
    public Result<String> updateMatchName(@PathVariable String id, @RequestBody CreateMatchRequest request) {
        return matchService.updateMatchName(id, request.getMatchName());
    }
}
