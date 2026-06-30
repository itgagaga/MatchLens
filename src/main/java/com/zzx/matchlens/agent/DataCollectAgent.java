package com.zzx.matchlens.agent;

import com.zzx.matchlens.entity.*;
import org.springframework.stereotype.Component;

import java.util.StringJoiner;

@Component
public class DataCollectAgent implements AiAgent {

    @Override
    public String execute(Match match) {
        return collect(match);
    }

    public String collect(Match match) {
        StringJoiner sj = new StringJoiner("\n");

        sj.add("=== 比赛数据摘要 ===");
        sj.add("比赛: " + match.getMatchName());
        sj.add("类型: " + match.getSportType());
        sj.add("状态: " + match.getStatus());

        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();
        if (home != null && away != null) {
            sj.add(String.format("比分: %s %d : %d %s",
                    home.getTeamName(), home.getScore(),
                    away.getScore(), away.getTeamName()));

            sj.add("\n--- 甲方球员数据 (" + home.getTeamName() + ") ---");
            appendTeamPlayers(sj, home);

            sj.add("\n--- 乙方球员数据 (" + away.getTeamName() + ") ---");
            appendTeamPlayers(sj, away);
        }

        sj.add("\n--- 事件时间线 (共 " + match.getEvents().size() + " 条) ---");
        for (MatchEvent e : match.getEvents()) {
            String desc = e.getDescription() != null ? e.getDescription() : "";
            sj.add(String.format("  [%s] %s - %s (球员: %s) %s",
                    e.getEventTime(), e.getEventType(),
                    e.getScoreValue() > 0 ? "+" + e.getScoreValue() + "分" : "",
                    e.getPlayerId(), desc));
        }

        MatchStatistics stats = match.getStatistics();
        if (stats != null) {
            sj.add("\n--- 汇总统计 ---");
            sj.add("总事件数: " + stats.getEventCount());
            sj.add("分差: " + stats.getScoreDifference());
            if (stats.getLeadingTeam() != null) {
                sj.add("领先方: " + stats.getLeadingTeam());
            }
        }

        return sj.toString();
    }

    private void appendTeamPlayers(StringJoiner sj, Team team) {
        for (Player p : team.getPlayers()) {
            StringBuilder line = new StringBuilder();
            line.append(String.format("  #%d %s", p.getNumber(), p.getPlayerName()));
            p.getStatistics().forEach((key, value) ->
                    line.append(String.format(" | %s: %d", key, value)));
            sj.add(line.toString());
        }
    }
}
