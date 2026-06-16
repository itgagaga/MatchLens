package com.zzx.matchlens.observer;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;
import com.zzx.matchlens.entity.Player;

public class StatisticsObserver implements MatchObserver {

    @Override
    public void onEventAdded(Match match, MatchEvent event) {
        match.getStatistics().updateFromMatch(match);

        Player player = findPlayer(match, event.getPlayerId());
        if (player != null) {
            String statKey = event.getEventType().name();
            player.addStat(statKey, event.getScoreValue() > 0 ? event.getScoreValue() : 1);
        }

        System.out.printf("[统计更新] 事件: %s | 球员: %s | 当前比分: %d:%d%n",
                event.getEventType(),
                player != null ? player.getPlayerName() : "未知",
                match.getHomeTeam().getScore(),
                match.getAwayTeam().getScore());
    }

    private Player findPlayer(Match match, String playerId) {
        Player p = match.getHomeTeam().findPlayer(playerId);
        if (p != null) return p;
        return match.getAwayTeam().findPlayer(playerId);
    }
}
