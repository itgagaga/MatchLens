package com.zzx.matchlens.chain;

import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.common.SportType;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;

public class ScoreCheckHandler extends EventCheckHandler {

    @Override
    protected Result<String> check(Match match, MatchEvent event) {
        if (event.getEventType() != EventType.SCORE) {
            return Result.ok("非得分事件，跳过分值校验");
        }

        int value = event.getScoreValue();
        if (value <= 0) {
            return Result.fail("得分值必须为正数");
        }

        SportType sport = match.getSportType();
        return switch (sport) {
            case BASKETBALL -> {
                if (value > 3) yield Result.fail("篮球单次得分不能超过3分");
                yield Result.ok("篮球得分校验通过");
            }
            case FOOTBALL -> {
                if (value != 1) yield Result.fail("足球进球得分只能是1");
                yield Result.ok("足球得分校验通过");
            }
            case VOLLEYBALL -> {
                if (value != 1) yield Result.fail("排球得分只能是1");
                yield Result.ok("排球得分校验通过");
            }
            default -> Result.ok("通用赛事得分校验通过");
        };
    }
}
