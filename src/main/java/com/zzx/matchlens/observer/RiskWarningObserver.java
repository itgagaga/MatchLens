package com.zzx.matchlens.observer;

import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;
import com.zzx.matchlens.entity.Player;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 风险预警观察者（观察者模式，优先级 3）。
 * <p>
 * 监听比赛事件，实时检测两类风险：
 * <ul>
 *   <li>球员犯规累积风险：当球员犯规次数达到阈值（4次）时发出预警</li>
 *   <li>比赛悬念下降风险：当分差达到 15 分以上时提示可能进入垃圾时间</li>
 * </ul>
 * </p>
 */
@Component
@Order(3)
public class RiskWarningObserver implements MatchObserver {

    /** 球员犯规预警阈值 */
    private static final int FOUL_WARNING_THRESHOLD = 4;

    @Override
    public void onEventAdded(Match match, MatchEvent event) {
        if (event.getEventType() == EventType.FOUL) {
            Player player = findPlayer(match, event.getPlayerId());
            if (player != null) {
                int fouls = player.getStat(EventType.FOUL.name());
                if (fouls >= FOUL_WARNING_THRESHOLD) {
                    System.out.printf("[风险预警] 球员 %s 犯规次数已达 %d 次，存在犯满离场风险！%n",
                            player.getPlayerName(), fouls);
                }
            }
        }

        int diff = Math.abs(match.getHomeTeam().getScore() - match.getAwayTeam().getScore());
        if (diff >= 15) {
            String leading = match.getHomeTeam().getScore() > match.getAwayTeam().getScore()
                    ? match.getHomeTeam().getTeamName()
                    : match.getAwayTeam().getTeamName();
            System.out.printf("[风险预警] %s 领先 %d 分，比赛可能进入垃圾时间%n", leading, diff);
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
        Player p = match.getHomeTeam().findPlayer(playerId);
        if (p != null) return p;
        return match.getAwayTeam().findPlayer(playerId);
    }
}
