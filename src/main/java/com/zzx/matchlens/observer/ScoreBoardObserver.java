package com.zzx.matchlens.observer;

import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;
import com.zzx.matchlens.entity.Team;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class ScoreBoardObserver implements MatchObserver {

    @Override
    public void onEventAdded(Match match, MatchEvent event) {
        if (event.getEventType() != EventType.SCORE) {
            return;
        }

        Team team;
        if (match.getHomeTeam().getTeamId().equals(event.getTeamId())) {
            team = match.getHomeTeam();
        } else if (match.getAwayTeam().getTeamId().equals(event.getTeamId())) {
            team = match.getAwayTeam();
        } else {
            return;
        }

        team.addScore(event.getScoreValue());

        System.out.printf("[比分板] %s 得分 +%d | %s %d : %d %s%n",
                team.getTeamName(),
                event.getScoreValue(),
                match.getHomeTeam().getTeamName(),
                match.getHomeTeam().getScore(),
                match.getAwayTeam().getScore(),
                match.getAwayTeam().getTeamName());
    }
}
