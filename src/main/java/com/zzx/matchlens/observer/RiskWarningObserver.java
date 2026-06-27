package com.zzx.matchlens.observer;

import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;
import com.zzx.matchlens.entity.Player;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class RiskWarningObserver implements MatchObserver {

    private static final int FOUL_WARNING_THRESHOLD = 4;

    @Override
    public void onEventAdded(Match match, MatchEvent event) {
        if (event.getEventType() == EventType.FOUL) {
            Player player = findPlayer(match, event.getPlayerId());
            if (player != null) {
                int fouls = player.getStat(EventType.FOUL.name());
                if (fouls >= FOUL_WARNING_THRESHOLD) {
                    System.out.printf("[风险预警] 球员 %s 犯规次数已达 %d 次，存在犯满离场风险！%n",
                            player.getPlayerName(), fouls);
                }
            }
        }

        int diff = Math.abs(match.getHomeTeam().getScore() - match.getAwayTeam().getScore());
        if (diff >= 15) {
            String leading = match.getHomeTeam().getScore() > match.getAwayTeam().getScore()
                    ? match.getHomeTeam().getTeamName()
                    : match.getAwayTeam().getTeamName();
            System.out.printf("[风险预警] %s 领先 %d 分，比赛可能进入垃圾时间%n", leading, diff);
        }
    }

    private Player findPlayer(Match match, String playerId) {
        Player p = match.getHomeTeam().findPlayer(playerId);
        if (p != null) return p;
        return match.getAwayTeam().findPlayer(playerId);
    }
}
