package com.zzx.matchlens.chain;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;

public class BasicEventCheckHandler extends EventCheckHandler {

    @Override
    protected Result<String> check(Match match, MatchEvent event) {
        if (match == null) {
            return Result.fail("比赛不存在");
        }
        if (event == null) {
            return Result.fail("事件数据为空");
        }
        if (event.getEventType() == null) {
            return Result.fail("事件类型不能为空");
        }
        return Result.ok("基础校验通过");
    }
}
