package com.zzx.matchlens.service;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.Player;
import com.zzx.matchlens.entity.Team;
import com.zzx.matchlens.repository.MatchRepository;
import com.zzx.matchlens.strategy.AnalysisStrategy;
import com.zzx.matchlens.strategy.AnalysisStrategyFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
public class ReportService {

    private final MatchRepository matchRepository;

    public ReportService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public String generateReport(String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("比赛不存在: " + matchId));

        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();

        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════╗\n");
        sb.append("║         比 赛 报 告                 ║\n");
        sb.append("╚══════════════════════════════════════╝\n\n");

        // 基本信息
        sb.append("【基本信息】\n");
        sb.append("比赛名称: ").append(match.getMatchName()).append("\n");
        sb.append("赛事类型: ").append(match.getSportType()).append("\n");
        sb.append("比赛状态: ").append(match.getStatus()).append("\n\n");

        // 比分
        sb.append("【最终比分】\n");
        sb.append(String.format("  %s  %d : %d  %s%n%n",
                home.getTeamName(), home.getScore(),
                away.getScore(), away.getTeamName()));

        // 胜负
        sb.append("【胜负结果】\n");
        if (home.getScore() > away.getScore()) {
            sb.append("  胜方: ").append(home.getTeamName()).append("\n");
        } else if (away.getScore() > home.getScore()) {
            sb.append("  胜方: ").append(away.getTeamName()).append("\n");
        } else {
            sb.append("  平局\n");
        }
        sb.append("\n");

        // 关键球员
        sb.append("【关键球员表现】\n");
        appendTopScorer(sb, home);
        appendTopScorer(sb, away);
        sb.append("\n");

        // 事件统计
        sb.append("【事件统计】\n");
        sb.append("  总事件数: ").append(match.getEvents().size()).append("\n\n");

        // 态势分析
        sb.append("【态势分析】\n");
        AnalysisStrategy strategy = AnalysisStrategyFactory.getStrategy(match.getSportType());
        sb.append(strategy.analyze(match)).append("\n\n");

        // 赛后复盘（占位，后续由 AI Agent 填充）
        sb.append("【赛后复盘】\n");
        sb.append(generateReview(match)).append("\n");

        return sb.toString();
    }

    private void appendTopScorer(StringBuilder sb, Team team) {
        team.getPlayers().stream()
                .max(Comparator.comparingInt(p -> p.getStat("SCORE")))
                .filter(p -> p.getStat("SCORE") > 0)
                .ifPresent(p -> sb.append(String.format("  %s #%d %s: %d 分%n",
                        team.getTeamName(), p.getNumber(), p.getPlayerName(),
                        p.getStat("SCORE"))));
    }

    private String generateReview(Match match) {
        // 占位实现，后续由 AI Agent 替换
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();
        StringBuilder sb = new StringBuilder();

        if (home.getScore() > away.getScore()) {
            sb.append(String.format("  本场比赛 %s 以 %d:%d 战胜 %s。%n",
                    home.getTeamName(), home.getScore(), away.getScore(), away.getTeamName()));
        } else if (away.getScore() > home.getScore()) {
            sb.append(String.format("  本场比赛 %s 以 %d:%d 战胜 %s。%n",
                    away.getTeamName(), away.getScore(), home.getScore(), home.getTeamName()));
        } else {
            sb.append(String.format("  本场比赛双方 %d:%d 战平。%n", home.getScore(), away.getScore()));
        }

        sb.append("  [赛后复盘内容将由 AI Agent 生成]");
        return sb.toString();
    }
}
