package com.zzx.matchlens.observer;

import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;
import com.zzx.matchlens.entity.Team;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 比分板观察者（观察者模式，优先级 1 / 最高）。
 * <p>
 * 监听比赛事件，当发生得分事件（{@link EventType#SCORE}）时，
 * 将对应分值累加到得分队伍的总分上，并输出实时比分信息。
 * 使用 {@code @Order(1)} 确保在所有观察者中最先执行，
 * 保证后续观察者能读取到最新的比分。
 * </p>
 */
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
