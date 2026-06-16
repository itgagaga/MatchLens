package com.zzx.matchlens.chain;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;

public abstract class EventCheckHandler {

    private EventCheckHandler next;

    public EventCheckHandler setNext(EventCheckHandler next) {
        this.next = next;
        return next;
    }

    public Result<String> handle(Match match, MatchEvent event) {
        Result<String> result = check(match, event);
        if (!result.isSuccess()) {
            return result;
        }
        if (next != null) {
            return next.handle(match, event);
        }
        return Result.ok("校验通过");
    }

    protected abstract Result<String> check(Match match, MatchEvent event);
}
