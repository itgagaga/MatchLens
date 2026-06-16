package com.zzx.matchlens.strategy;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.Team;

import java.util.StringJoiner;

public class GeneralAnalysisStrategy implements AnalysisStrategy {

    @Override
    public String analyze(Match match) {
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();
        int diff = Math.abs(home.getScore() - away.getScore());

        StringJoiner sj = new StringJoiner("\n");
        sj.add("【通用赛事态势分析】");
        sj.add(String.format("当前比分: %s %d : %d %s", home.getTeamName(), home.getScore(),
                away.getScore(), away.getTeamName()));

        if (diff == 0) {
            sj.add("双方战平。");
        } else {
            String leading = home.getScore() > away.getScore() ? home.getTeamName() : away.getTeamName();
            sj.add(String.format("%s 领先 %d 分。", leading, diff));
        }

        sj.add(String.format("已记录事件数: %d", match.getEvents().size()));

        return sj.toString();
    }
}
