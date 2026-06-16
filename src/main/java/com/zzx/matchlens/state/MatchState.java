package com.zzx.matchlens.state;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.common.Result;

public interface MatchState {

    Result<String> start(Match match);

    Result<String> pause(Match match);

    Result<String> resume(Match match);

    Result<String> finish(Match match);

    Result<String> addEvent(Match match);

    Result<String> modifyTeams(Match match);

    Result<String> viewReport(Match match);
}
