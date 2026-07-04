package com.zzx.matchlens.state;

import com.zzx.matchlens.common.MatchStatus;
import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.Match;

public class FinishedState implements MatchState {

    @Override
    public Result<String> start(Match match) {
        match.setStatus(MatchStatus.RUNNING);
        return Result.ok("比赛已重新开启");
    }

    @Override
    public Result<String> pause(Match match) {
        match.setStatus(MatchStatus.PAUSED);
        return Result.ok("比赛已切换为暂停状态");
    }

    @Override
    public Result<String> resume(Match match) {
        match.setStatus(MatchStatus.RUNNING);
        return Result.ok("比赛已恢复为进行中");
    }

    @Override
    public Result<String> finish(Match match) {
        return Result.fail("比赛已经结束");
    }

    @Override
    public Result<String> addEvent(Match match) {
        match.setStatus(MatchStatus.RUNNING);
        return Result.ok("比赛已自动重新开启，可以继续录入事件");
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
