package com.zzx.matchlens.agent;

import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.entity.*;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.StringJoiner;

/**
 * 本地规则智能体。
 * <p>
 * 当远程 AI 模型不可用时，作为降级方案使用本地规则模板生成赛后复盘报告。
 * 基于预设的规则和阈值，分析比赛结果、关键球员、事件统计、
 * 胜负原因并给出改进建议。生成的报告格式固定但无需依赖外部服务。
 * </p>
 */
@Component
public class LocalRuleAgent implements AiAgent {

    @Override
    public String execute(Match match) {
        return generateReview(match);
    }

    /**
     * 基于本地规则模板生成赛后复盘报告。
     * <p>
     * 报告包含：比赛结果、关键球员表现、关键事件回顾、
     * 胜负原因分析和改进建议。
     * </p>
     *
     * @param match 待分析的比赛实体
     * @return 规则模板生成的复盘报告文本
     */
    public String generateReview(Match match) {
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();
        StringJoiner sj = new StringJoiner("\n");

        // 比赛结果
        String winner;
        if (home.getScore() > away.getScore()) {
            winner = home.getTeamName();
            sj.add(String.format("本场比赛 %s 以 %d:%d 战胜 %s。",
                    winner, home.getScore(), away.getScore(), away.getTeamName()));
        } else if (away.getScore() > home.getScore()) {
            winner = away.getTeamName();
            sj.add(String.format("本场比赛 %s 以 %d:%d 战胜 %s。",
                    winner, away.getScore(), home.getScore(), home.getTeamName()));
        } else {
            winner = null;
            sj.add(String.format("本场比赛双方 %d:%d 战平。", home.getScore(), away.getScore()));
        }

        // 关键球员表现
        sj.add("\n【关键球员表现】");
        appendTopScorer(sj, home);
        appendTopScorer(sj, away);

        // 关键事件回顾
        sj.add("\n【关键事件回顾】");
        long scoreEvents = match.getEvents().stream()
                .filter(e -> e.getEventType() == EventType.SCORE)
                .count();
        long foulEvents = match.getEvents().stream()
                .filter(e -> e.getEventType() == EventType.FOUL)
                .count();
        sj.add(String.format("全场比赛共 %d 个事件，其中得分事件 %d 次，犯规事件 %d 次。",
                match.getEvents().size(), scoreEvents, foulEvents));

        // 胜负原因分析
        sj.add("\n【胜负原因分析】");
        if (winner != null) {
            int diff = Math.abs(home.getScore() - away.getScore());
            if (diff >= 15) {
                sj.add(winner + " 以较大优势获胜，展现出明显的实力差距。");
            } else if (diff >= 5) {
                sj.add(winner + " 稳扎稳打取得胜利，整体发挥更加稳定。");
            } else {
                sj.add(winner + " 险胜对手，比赛过程紧张激烈，胜负在毫厘之间。");
            }

            Team winnerTeam = winner.equals(home.getTeamName()) ? home : away;
            int winnerFouls = teamFouls(winnerTeam);
            int loserFouls = teamFouls(winner.equals(home.getTeamName()) ? away : home);
            if (winnerFouls < loserFouls) {
                sj.add("胜方犯规次数较少（" + winnerFouls + " vs " + loserFouls + "），纪律性更好。");
            }
        } else {
            sj.add("双方实力接近，比赛以平局收场。");
        }

        // 改进建议
        sj.add("\n【改进建议】");
        appendSuggestions(sj, home, away, winner);

        return sj.toString();
    }

    /**
     * 追加队伍的最佳射手信息到文本拼接器。
     *
     * @param sj   文本拼接器
     * @param team 队伍实体
     */
    private void appendTopScorer(StringJoiner sj, Team team) {
        team.getPlayers().stream()
                .max(Comparator.comparingInt(p -> p.getStat("SCORE")))
                .filter(p -> p.getStat("SCORE") > 0)
                .ifPresent(p -> sj.add(String.format("  %s #%d %s: %d 分",
                        team.getTeamName(), p.getNumber(), p.getPlayerName(),
                        p.getStat("SCORE"))));
    }

    /**
     * 计算队伍的全员犯规总数。
     *
     * @param team 队伍实体
     * @return 犯规总次数
     */
    private int teamFouls(Team team) {
        return team.getPlayers().stream()
                .mapToInt(p -> p.getStat(EventType.FOUL.name()))
                .sum();
    }

    /**
     * 根据比赛情况追加针对性的改进建议。
     *
     * @param sj     文本拼接器
     * @param home   甲方队伍
     * @param away   乙方队伍
     * @param winner 获胜方队名，平局时为 null
     */
    private void appendSuggestions(StringJoiner sj, Team home, Team away, String winner) {
        int homeFouls = teamFouls(home);
        int awayFouls = teamFouls(away);

        if (homeFouls >= 8) {
            sj.add("  - " + home.getTeamName() + " 犯规较多（" + homeFouls + " 次），需加强防守纪律。");
        }
        if (awayFouls >= 8) {
            sj.add("  - " + away.getTeamName() + " 犯规较多（" + awayFouls + " 次），需加强防守纪律。");
        }

        int diff = Math.abs(home.getScore() - away.getScore());
        if (diff <= 5) {
            sj.add("  - 比分接近，建议两队在关键时刻加强心理素质训练。");
        }

        if (winner == null) {
            sj.add("  - 双方需在进攻效率上寻求突破，争取在下次交锋中分出胜负。");
        }
    }
}
