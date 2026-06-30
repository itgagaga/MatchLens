package com.zzx.matchlens.service;

import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;
import com.zzx.matchlens.entity.Player;
import com.zzx.matchlens.repository.MatchRepository;
import org.springframework.stereotype.Service;

/**
 * 统计重算服务
 *
 * 事件修改/删除属于数据修正功能，必须触发统计重算，
 * 避免人工误录导致比分和统计不一致。
 * 采用"全量重算"策略而非减法修正，保证数据一致性。
 */
@Service
public class StatisticsRebuildService {

    private final MatchRepository matchRepository;

    public StatisticsRebuildService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    /**
     * 重算某场比赛的全部统计：
     * 1. 清空球员统计
     * 2. 重置双方 score 为 0
     * 3. 按 event_time 升序重放所有事件
     * 4. 更新 t_team.score 和 t_player_statistics
     */
    public void rebuildStatistics(String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("比赛不存在: " + matchId));

        // 1. 清空球员统计
        match.clearPlayerStatistics();

        // 2. 重置双方分数
        match.getHomeTeam().setScore(0);
        match.getAwayTeam().setScore(0);

        // 3 & 4. 按 event_time 升序重放所有事件（events 已由 repository 按 eventTime 升序加载）
        for (MatchEvent event : match.getEvents()) {
            replayEvent(match, event);
        }

        // 5. 更新统计快照
        match.getStatistics().updateFromMatch(match);

        // 6. 持久化
        matchRepository.save(match);
    }

    private void replayEvent(Match match, MatchEvent event) {
        // 更新球队比分
        if (event.getEventType() == EventType.SCORE) {
            if (match.getHomeTeam().getTeamId().equals(event.getTeamId())) {
                match.getHomeTeam().addScore(event.getScoreValue());
            } else if (match.getAwayTeam().getTeamId().equals(event.getTeamId())) {
                match.getAwayTeam().addScore(event.getScoreValue());
            }
        }

        // 更新球员统计
        Player player = findPlayer(match, event.getPlayerId());
        if (player != null) {
            String statKey = event.getEventType().name();
            int statValue = (event.getEventType() == EventType.SCORE && event.getScoreValue() > 0)
                    ? event.getScoreValue() : 1;
            player.addStat(statKey, statValue);
        }
    }

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
}
