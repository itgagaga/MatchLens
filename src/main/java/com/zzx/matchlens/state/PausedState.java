package com.zzx.matchlens.state;

import com.zzx.matchlens.common.MatchStatus;
import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.Match;

public class PausedState implements MatchState {

    @Override
    public Result<String> start(Match match) {
        return Result.fail("比赛已暂停，请使用恢复操作");
    }

    @Override
    public Result<String> pause(Match match) {
        return Result.fail("比赛已经处于暂停状态");
    }

    @Override
    public Result<String> resume(Match match) {
        match.setStatus(MatchStatus.RUNNING);
        return Result.ok("比赛已恢复");
    }

    @Override
    public Result<String> finish(Match match) {
        match.setStatus(MatchStatus.FINISHED);
        return Result.ok("比赛已结束");
    }

    @Override
    public Result<String> addEvent(Match match) {
        return Result.fail("比赛暂停中，无法录入事件");
    }

    @Override
    public Result<String> modifyTeams(Match match) {
        return Result.fail("比赛暂停中，无法修改队伍信息");
    }

    @Override
    public Result<String> viewReport(Match match) {
        return Result.fail("比赛尚未结束，无法查看完整报告");
    }
}
