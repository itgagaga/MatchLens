package com.zzx.matchlens.agent;

import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.entity.*;
import org.springframework.stereotype.Component;

import java.util.StringJoiner;

/**
 * 数据采集智能体。
 * <p>
 * 负责从比赛实体中采集并整理结构化数据，生成供 AI 模型分析使用的文本摘要。
 * 数据包括比赛基本信息、比分、球员统计、事件时间线及汇总统计。
 * 在采集前会从事件重算球员统计和比分，确保数据与事件时间线严格一致。
 * </p>
 */
@Component
public class DataCollectAgent implements AiAgent {

    @Override
    public String execute(Match match) {
        return collect(match);
    }

    /**
     * 采集比赛数据并生成结构化文本摘要。
     * <p>
     * 流程：先重算统计和球员数据，然后按顺序拼接比赛基本信息、
     * 双方球员数据、事件时间线和汇总统计。
     * </p>
     *
     * @param match 待采集的比赛实体
     * @return 结构化数据文本
     */
    public String collect(Match match) {
        // 统计，确保比分和球员数据
        if (match.getStatistics() != null && match.getHomeTeam() != null && match.getAwayTeam() != null) {
            // 从事件重算球员统计，避免 DB 中的旧统计与事件不一致
            rebuildPlayerStatsFromEvents(match);
            match.getStatistics().updateFromMatch(match);
        }

        StringJoiner sj = new StringJoiner("\n");

        sj.add("=== 比赛数据摘要 ===");
        sj.add("比赛: " + match.getMatchName());
        sj.add("类型: " + match.getSportType());
        sj.add("状态: " + match.getStatus());

        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();
        if (home != null && away != null) {
            // 使用统计计算的真实比分，而非 team.score 种子值
            int homeScore = match.getStatistics() != null ? match.getStatistics().getHomeScore() : home.getScore();
            int awayScore = match.getStatistics() != null ? match.getStatistics().getAwayScore() : away.getScore();
            sj.add(String.format("比分: %s %d : %d %s",
                    home.getTeamName(), homeScore,
                    awayScore, away.getTeamName()));

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

    /**
     * 从事件列表重算所有球员统计（不持久化），
     * 确保喂给 AI 的球员数据与事件时间线严格一致。
     */
    private void rebuildPlayerStatsFromEvents(Match match) {
        match.clearPlayerStatistics();
        for (MatchEvent event : match.getEvents()) {
            Player player = findPlayer(match, event.getPlayerId());
            if (player != null) {
                String statKey = event.getEventType().name();
                int statValue = (event.getEventType() == EventType.SCORE && event.getScoreValue() > 0)
                        ? event.getScoreValue() : 1;
                player.addStat(statKey, statValue);
            }
        }
    }

    /**
     * 在比赛的双方队伍中查找指定 ID 的球员。
     *
     * @param match    比赛实体
     * @param playerId 球员 ID
     * @return 找到的球员对象，未找到返回 null
     */
    private Player findPlayer(Match match, String playerId) {
        if (match.getHomeTeam() != null) {
            Player p = match.getHomeTeam().findPlayer(playerId);
            if (p != null) return p;
        }
        if (match.getAwayTeam() != null) {
            return match.getAwayTeam().findPlayer(playerId);
        }
        return null;
    }

    /**
     * 将指定队伍的所有球员数据追加到文本拼接器中。
     *
     * @param sj   文本拼接器
     * @param team 队伍实体
     */
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
