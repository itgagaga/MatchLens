package com.zzx.matchlens.service;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.common.SportType;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.Player;
import com.zzx.matchlens.entity.Team;
import com.zzx.matchlens.repository.MatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MatchService {

    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public Match createMatch(String matchName, SportType sportType) {
        Match match = new Match(UUID.randomUUID().toString(), matchName, sportType);
        return matchRepository.save(match);
    }

    public Result<String> setTeams(String matchId, String teamAName, String teamBName) {
        Match match = getMatch(matchId);
        Result<String> check = match.canModifyTeams();
        if (!check.isSuccess()) {
            return Result.fail(check.getMessage());
        }

        Team teamA = new Team(UUID.randomUUID().toString(), teamAName);
        Team teamB = new Team(UUID.randomUUID().toString(), teamBName);
        match.setHomeTeam(teamA);
        match.setAwayTeam(teamB);
        matchRepository.save(match);
        return Result.ok("队伍设置成功");
    }

    public Result<String> addPlayer(String matchId, boolean isTeamA, String playerName, int number) {
        Match match = getMatch(matchId);
        Result<String> check = match.canModifyTeams();
        if (!check.isSuccess()) {
            return Result.fail(check.getMessage());
        }

        Team team = isTeamA ? match.getHomeTeam() : match.getAwayTeam();
        if (team == null) {
            return Result.fail("请先设置队伍");
        }

        Player player = new Player(UUID.randomUUID().toString(), playerName, team.getTeamId(), number);
        team.addPlayer(player);
        matchRepository.save(match);
        return Result.ok("球员 " + playerName + " 添加成功");
    }

    public Result<String> startMatch(String matchId) {
        Match match = getMatch(matchId);
        Result<String> result = match.start();
        if (result.isSuccess()) {
            matchRepository.save(match);
            return Result.ok("比赛已开始");
        }
        return Result.fail(result.getMessage());
    }

    public Result<String> pauseMatch(String matchId) {
        Match match = getMatch(matchId);
        Result<String> result = match.pause();
        if (result.isSuccess()) {
            matchRepository.save(match);
            return Result.ok("比赛已暂停");
        }
        return Result.fail(result.getMessage());
    }

    public Result<String> resumeMatch(String matchId) {
        Match match = getMatch(matchId);
        Result<String> result = match.resume();
        if (result.isSuccess()) {
            matchRepository.save(match);
            return Result.ok("比赛已恢复");
        }
        return Result.fail(result.getMessage());
    }

    public Result<String> finishMatch(String matchId) {
        Match match = getMatch(matchId);
        Result<String> result = match.finish();
        if (result.isSuccess()) {
            matchRepository.save(match);
            return Result.ok("比赛已结束");
        }
        return Result.fail(result.getMessage());
    }

    public Match getMatch(String matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("比赛不存在: " + matchId));
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public Result<String> deleteMatch(String matchId) {
        getMatch(matchId);
        matchRepository.delete(matchId);
        return Result.ok("比赛已删除");
    }

    public Result<String> updateMatchName(String matchId, String matchName) {
        Match match = getMatch(matchId);
        match.setMatchName(matchName);
        matchRepository.save(match);
        return Result.ok("比赛名称已更新");
    }
}
