package com.zzx.matchlens.strategy;

import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.Team;

import java.util.StringJoiner;

/**
 * 足球分析策略。
 * <p>
 * 针对足球赛事的本地态势分析实现，分析维度包括：
 * 比分与领先、红黄牌统计、犯规统计等。
 * </p>
 */
public class FootballAnalysisStrategy implements AnalysisStrategy {

    @Override
    public String analyze(Match match) {
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();
        int diff = Math.abs(home.getScore() - away.getScore());

        StringJoiner sj = new StringJoiner("\n");
        sj.add("【足球态势分析】");
        sj.add(String.format("当前比分: %s %d : %d %s", home.getTeamName(), home.getScore(),
                away.getScore(), away.getTeamName()));

        if (diff == 0) {
            sj.add("双方战平。");
        } else {
            String leading = home.getScore() > away.getScore() ? home.getTeamName() : away.getTeamName();
            sj.add(String.format("%s 领先 %d 球。", leading, diff));
        }

        // 红黄牌
        int homeYellow = teamStat(home, EventType.YELLOW_CARD.name());
        int awayYellow = teamStat(away, EventType.YELLOW_CARD.name());
        int homeRed = teamStat(home, EventType.RED_CARD.name());
        int awayRed = teamStat(away, EventType.RED_CARD.name());

        if (homeYellow + awayYellow + homeRed + awayRed > 0) {
            sj.add(String.format("黄牌: %s %d, %s %d", home.getTeamName(), homeYellow,
                    away.getTeamName(), awayYellow));
            sj.add(String.format("红牌: %s %d, %s %d", home.getTeamName(), homeRed,
                    away.getTeamName(), awayRed));
            if (homeRed > 0) sj.add(home.getTeamName() + " 有球员被罚下，人数劣势！");
            if (awayRed > 0) sj.add(away.getTeamName() + " 有球员被罚下，人数劣势！");
        }

        // 犯规
        int homeFouls = teamStat(home, EventType.FOUL.name());
        int awayFouls = teamStat(away, EventType.FOUL.name());
        if (homeFouls + awayFouls > 0) {
            sj.add(String.format("犯规: %s %d, %s %d", home.getTeamName(), homeFouls,
                    away.getTeamName(), awayFouls));
        }

        return sj.toString();
    }

    /**
     * 计算队伍指定统计项的全员总和。
     *
     * @param team    队伍实体
     * @param statKey 统计项键名
     * @return 统计值总和
     */
    private int teamStat(Team team, String statKey) {
        return team.getPlayers().stream()
                .mapToInt(p -> p.getStat(statKey))
                .sum();
    }
}
