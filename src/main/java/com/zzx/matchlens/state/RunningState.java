package com.zzx.matchlens.state;

import com.zzx.matchlens.common.MatchStatus;
import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.Match;

public class RunningState implements MatchState {

    @Override
    public Result<String> start(Match match) {
        return Result.fail("比赛已在进行中");
    }

    @Override
    public Result<String> pause(Match match) {
        match.setStatus(MatchStatus.PAUSED);
        return Result.ok("比赛已暂停");
    }

    @Override
    public Result<String> resume(Match match) {
        return Result.fail("比赛正在进行中，无需恢复");
    }

    @Override
    public Result<String> finish(Match match) {
        match.setStatus(MatchStatus.FINISHED);
        return Result.ok("比赛已结束");
    }

    @Override
    public Result<String> addEvent(Match match) {
        return Result.ok("可以录入事件");
    }

    @Override
    public Result<String> modifyTeams(Match match) {
        return Result.fail("比赛进行中，无法修改队伍信息");
    }

    @Override
    public Result<String> viewReport(Match match) {
        return Result.fail("比赛尚未结束，无法查看完整报告");
    }
}
