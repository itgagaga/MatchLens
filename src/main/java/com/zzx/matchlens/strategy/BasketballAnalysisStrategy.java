package com.zzx.matchlens.strategy;

import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.Player;
import com.zzx.matchlens.entity.Team;

import java.util.Comparator;
import java.util.StringJoiner;

/**
 * 篮球分析策略。
 * <p>
 * 针对篮球赛事的本地态势分析实现，分析维度包括：
 * 比分与分差、连续得分势头、犯规统计、关键球员表现等。
 * </p>
 */
public class BasketballAnalysisStrategy implements AnalysisStrategy {

    @Override
    public String analyze(Match match) {
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();
        int diff = Math.abs(home.getScore() - away.getScore());

        StringJoiner sj = new StringJoiner("\n");
        sj.add("【篮球态势分析】");
        sj.add(String.format("当前比分: %s %d : %d %s", home.getTeamName(), home.getScore(),
                away.getScore(), away.getTeamName()));

        // 领先方与分差
        if (diff == 0) {
            sj.add("双方战平，比赛胶着。");
        } else {
            String leading = home.getScore() > away.getScore() ? home.getTeamName() : away.getTeamName();
            sj.add(String.format("%s 领先 %d 分。", leading, diff));
            if (diff >= 15) {
                sj.add("分差较大，比赛可能失去悬念。");
            } else if (diff <= 5) {
                sj.add("分差很小，比赛进入焦灼阶段。");
            }
        }

        // 连续得分分析
        analyzeConsecutive(match, sj);

        // 犯规分析
        int homeFouls = teamStat(home, EventType.FOUL.name());
        int awayFouls = teamStat(away, EventType.FOUL.name());
        if (homeFouls > 0 || awayFouls > 0) {
            sj.add(String.format("犯规: %s %d 次, %s %d 次", home.getTeamName(), homeFouls,
                    away.getTeamName(), awayFouls));
            if (homeFouls >= 8) sj.add(home.getTeamName() + " 犯规过多，防守压力增大。");
            if (awayFouls >= 8) sj.add(away.getTeamName() + " 犯规过多，防守压力增大。");
        }

        // 关键球员
        Player topScorer = findTopScorer(match);
        if (topScorer != null) {
            sj.add(String.format("关键球员: %s (#%d) 贡献 %d 分",
                    topScorer.getPlayerName(), topScorer.getNumber(),
                    topScorer.getStat(EventType.SCORE.name())));
        }

        return sj.toString();
    }

    /**
     * 分析最近连续得分情况，若同一队伍连续得分 3 次及以上则提示势头强劲。
     *
     * @param match 比赛实体
     * @param sj    文本拼接器
     */
    private void analyzeConsecutive(Match match, StringJoiner sj) {
        var events = match.getEvents();
        if (events.size() < 3) return;

        int homeStreak = 0, awayStreak = 0;
        for (int i = events.size() - 1; i >= 0; i--) {
            var e = events.get(i);
            if (e.getEventType() != EventType.SCORE) break;
            if (e.getTeamId().equals(match.getHomeTeam().getTeamId())) {
                if (awayStreak > 0) break;
                homeStreak++;
            } else {
                if (homeStreak > 0) break;
                awayStreak++;
            }
        }

        if (homeStreak >= 3) {
            sj.add(match.getHomeTeam().getTeamName() + " 连续得分 " + homeStreak + " 次，势头强劲！");
        }
        if (awayStreak >= 3) {
            sj.add(match.getAwayTeam().getTeamName() + " 连续得分 " + awayStreak + " 次，势头强劲！");
        }
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

    /**
     * 在双方队伍中查找得分最高的球员。
     *
     * @param match 比赛实体
     * @return 得分最高的球员，无得分者时返回 null
     */
    private Player findTopScorer(Match match) {
        java.util.List<Player> allPlayers = new java.util.ArrayList<>();
        allPlayers.addAll(match.getHomeTeam().getPlayers());
        allPlayers.addAll(match.getAwayTeam().getPlayers());
        return allPlayers.stream()
                .collect(java.util.stream.Collectors.toMap(
                        p -> p, p -> p.getStat(EventType.SCORE.name())))
                .entrySet().stream()
                .max(Comparator.comparingInt(java.util.Map.Entry::getValue))
                .filter(e -> e.getValue() > 0)
                .map(java.util.Map.Entry::getKey)
                .orElse(null);
    }
}
