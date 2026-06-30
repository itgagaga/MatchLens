package com.zzx.matchlens.state;

import com.zzx.matchlens.common.MatchStatus;
import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.Match;

public class NotStartedState implements MatchState {

    @Override
    public Result<String> start(Match match) {
        if (match.getHomeTeam() == null || match.getAwayTeam() == null) {
            return Result.fail("比赛必须设置甲方和乙方才能开始");
        }
        match.setStatus(MatchStatus.RUNNING);
        return Result.ok("比赛已开始");
    }

    @Override
    public Result<String> pause(Match match) {
        return Result.fail("比赛尚未开始，无法暂停");
    }

    @Override
    public Result<String> resume(Match match) {
        return Result.fail("比赛尚未开始，无法恢复");
    }

    @Override
    public Result<String> finish(Match match) {
        return Result.fail("比赛尚未开始，无法结束");
    }

    @Override
    public Result<String> addEvent(Match match) {
        return Result.fail("比赛尚未开始，无法录入事件");
    }

    @Override
    public Result<String> modifyTeams(Match match) {
        return Result.ok("可以修改队伍信息");
    }

    @Override
    public Result<String> viewReport(Match match) {
        return Result.fail("比赛尚未开始，无法查看报告");
    }
}
