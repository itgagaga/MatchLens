package com.zzx.matchlens.chain;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;
import com.zzx.matchlens.entity.Player;

public class PlayerCheckHandler extends EventCheckHandler {

    @Override
    protected Result<String> check(Match match, MatchEvent event) {
        String teamId = event.getTeamId();
        String playerId = event.getPlayerId();

        if (teamId == null || playerId == null) {
            return Result.fail("事件必须指定队伍和球员");
        }

        boolean isHome = match.getHomeTeam().getTeamId().equals(teamId);
        boolean isAway = match.getAwayTeam().getTeamId().equals(teamId);

        if (!isHome && !isAway) {
            return Result.fail("队伍 " + teamId + " 不属于当前比赛");
        }

        Player player = isHome
                ? match.getHomeTeam().findPlayer(playerId)
                : match.getAwayTeam().findPlayer(playerId);

        if (player == null) {
            return Result.fail("球员 " + playerId + " 不属于队伍 " + teamId);
        }

        return Result.ok("球员校验通过");
    }
}
