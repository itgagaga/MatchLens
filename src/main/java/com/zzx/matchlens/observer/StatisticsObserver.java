package com.zzx.matchlens.observer;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;
import com.zzx.matchlens.entity.Player;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class StatisticsObserver implements MatchObserver {

    @Override
    public void onEventAdded(Match match, MatchEvent event) {
        // updateFromMatch 已从事件重放重建所有球员统计，无需再单独 addStat
        match.getStatistics().updateFromMatch(match);

        Player player = findPlayer(match, event.getPlayerId());

        System.out.printf("[统计更新] 事件: %s | 球员: %s | 当前比分: %d:%d%n",
                event.getEventType(),
                player != null ? player.getPlayerName() : "未知",
                match.getStatistics().getHomeScore(),
                match.getStatistics().getAwayScore());
    }

    private Player findPlayer(Match match, String playerId) {
        Player p = match.getHomeTeam().findPlayer(playerId);
        if (p != null) return p;
        return match.getAwayTeam().findPlayer(playerId);
    }
}
