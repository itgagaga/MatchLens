package com.zzx.matchlens.observer;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;
import com.zzx.matchlens.entity.Player;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 统计更新观察者（观察者模式，优先级 2）。
 * <p>
 * 监听比赛事件，每次有新事件时重新计算比赛统计数据
 * （通过 {@link com.zzx.matchlens.entity.MatchStatistics#updateFromMatch}），
 * 并输出事件类型、球员名称和当前比分等信息。
 * 使用 {@code @Order(2)} 确保在比分板之后、风险预警和复盘记录之前执行。
 * </p>
 */
@Component
@Order(2)
public class StatisticsObserver implements MatchObserver {

    @Override
    public void onEventAdded(Match match, MatchEvent event) {
        // updateFromMatch 已从事件重放重建所有球员统计，无需再单独 addStat
        match.getStatistics().updateFromMatch(match);

        Player player = findPlayer(match, event.getPlayerId());

        System.out.printf("[统计更新] 事件: %s | 球员: %s | 当前比分: %d:%d%n",
                event.getEventType(),
                player != null ? player.getPlayerName() : "未知",
                match.getStatistics().getHomeScore(),
                match.getStatistics().getAwayScore());
    }

    /**
     * 在比赛的双方队伍中查找指定 ID 的球员。
     *
     * @param match    比赛实体
     * @param playerId 球员 ID
     * @return 找到的球员对象，未找到返回 null
     */
    private Player findPlayer(Match match, String playerId) {
        Player p = match.getHomeTeam().findPlayer(playerId);
        if (p != null) return p;
        return match.getAwayTeam().findPlayer(playerId);
    }
}
