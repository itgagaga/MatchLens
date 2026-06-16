package com.zzx.matchlens.state;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.Match;

public class FinishedState implements MatchState {

    @Override
    public Result<String> start(Match match) {
        return Result.fail("比赛已结束，无法重新开始");
    }

    @Override
    public Result<String> pause(Match match) {
        return Result.fail("比赛已结束，无法暂停");
    }

    @Override
    public Result<String> resume(Match match) {
        return Result.fail("比赛已结束，无法恢复");
    }

    @Override
    public Result<String> finish(Match match) {
        return Result.fail("比赛已经结束");
    }

    @Override
    public Result<String> addEvent(Match match) {
        return Result.fail("比赛已结束，无法录入事件");
    }

    @Override
    public Result<String> modifyTeams(Match match) {
        return Result.fail("比赛已结束，无法修改队伍信息");
    }

    @Override
    public Result<String> viewReport(Match match) {
        return Result.ok("可以查看报告");
    }
}
