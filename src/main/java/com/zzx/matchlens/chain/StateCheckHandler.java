package com.zzx.matchlens.chain;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;

public class StateCheckHandler extends EventCheckHandler {

    @Override
    protected Result<String> check(Match match, MatchEvent event) {
        return match.canAddEvent();
    }
}
