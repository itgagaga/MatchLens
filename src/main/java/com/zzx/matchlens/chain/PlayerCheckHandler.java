package com.zzx.matchlens.chain;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;
import com.zzx.matchlens.entity.Player;

/**
 * 球员校验处理器（责任链第三环）。
 * <p>
 * 校验事件中指定的队伍和球员是否合法：
 * 队伍必须属于当前比赛（甲方或乙方），球员必须属于指定队伍。
 * 防止录入不属于比赛的队伍或球员的事件。
 * </p>
 */
public class PlayerCheckHandler extends EventCheckHandler {

    /**
     * 校验事件中的队伍和球员归属关系。
     *
     * @param match 比赛实体
     * @param event 待校验的比赛事件
     * @return 校验结果
     */
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
