package com.zzx.matchlens.service;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchStatistics;
import com.zzx.matchlens.entity.Player;
import com.zzx.matchlens.entity.Team;
import com.zzx.matchlens.repository.MatchRepository;
import com.zzx.matchlens.strategy.AnalysisStrategy;
import com.zzx.matchlens.strategy.AnalysisStrategyFactory;
import org.springframework.stereotype.Service;

@Service
public class StatisticsService {

    private final MatchRepository matchRepository;

    public StatisticsService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public MatchStatistics getStatistics(String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("比赛不存在: " + matchId));
        match.getStatistics().updateFromMatch(match);
        return match.getStatistics();
    }

    public String getAnalysis(String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("比赛不存在: " + matchId));
        AnalysisStrategy strategy = AnalysisStrategyFactory.getStrategy(match.getSportType());
        return strategy.analyze(match);
    }

    public void printMatchOverview(String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("比赛不存在: " + matchId));

        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();

        System.out.println("========== 比赛概况 ==========");
        System.out.println("比赛: " + match.getMatchName());
        System.out.println("状态: " + match.getStatus());
        System.out.printf("比分: %s %d : %d %s%n",
                home.getTeamName(), home.getScore(),
                away.getScore(), away.getTeamName());

        System.out.println("\n--- " + home.getTeamName() + " 球员 ---");
        for (Player p : home.getPlayers()) {
            System.out.printf("  #%d %s%n", p.getNumber(), p.getPlayerName());
            p.getStatistics().forEach((k, v) -> System.out.printf("    %s: %d%n", k, v));
        }

        System.out.println("\n--- " + away.getTeamName() + " 球员 ---");
        for (Player p : away.getPlayers()) {
            System.out.printf("  #%d %s%n", p.getNumber(), p.getPlayerName());
            p.getStatistics().forEach((k, v) -> System.out.printf("    %s: %d%n", k, v));
        }

        System.out.println("\n--- 态势分析 ---");
        System.out.println(getAnalysis(matchId));
        System.out.println("==============================");
    }
}
