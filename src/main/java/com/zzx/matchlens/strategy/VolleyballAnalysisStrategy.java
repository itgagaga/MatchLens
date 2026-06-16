package com.zzx.matchlens.strategy;

import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.Team;

import java.util.StringJoiner;

public class VolleyballAnalysisStrategy implements AnalysisStrategy {

    @Override
    public String analyze(Match match) {
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();
        int diff = Math.abs(home.getScore() - away.getScore());

        StringJoiner sj = new StringJoiner("\n");
        sj.add("【排球态势分析】");
        sj.add(String.format("当前比分: %s %d : %d %s", home.getTeamName(), home.getScore(),
                away.getScore(), away.getTeamName()));

        if (diff == 0) {
            sj.add("双方战平。");
        } else {
            String leading = home.getScore() > away.getScore() ? home.getTeamName() : away.getTeamName();
            sj.add(String.format("%s 领先 %d 分。", leading, diff));
        }

        // 发球得分
        int homeServe = teamStat(home, EventType.SERVE_ACE.name());
        int awayServe = teamStat(away, EventType.SERVE_ACE.name());
        if (homeServe + awayServe > 0) {
            sj.add(String.format("发球得分: %s %d, %s %d", home.getTeamName(), homeServe,
                    away.getTeamName(), awayServe));
        }

        // 拦网
        int homeBlock = teamStat(home, EventType.BLOCK.name());
        int awayBlock = teamStat(away, EventType.BLOCK.name());
        if (homeBlock + awayBlock > 0) {
            sj.add(String.format("拦网: %s %d, %s %d", home.getTeamName(), homeBlock,
                    away.getTeamName(), awayBlock));
        }

        // 失误
        int homeError = teamStat(home, EventType.ERROR.name());
        int awayError = teamStat(away, EventType.ERROR.name());
        if (homeError + awayError > 0) {
            sj.add(String.format("失误: %s %d, %s %d", home.getTeamName(), homeError,
                    away.getTeamName(), awayError));
            if (homeError >= 5) sj.add(home.getTeamName() + " 失误偏多，需要调整。");
            if (awayError >= 5) sj.add(away.getTeamName() + " 失误偏多，需要调整。");
        }

        return sj.toString();
    }

    private int teamStat(Team team, String statKey) {
        return team.getPlayers().stream()
                .mapToInt(p -> p.getStat(statKey))
                .sum();
    }
}
