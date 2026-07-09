package com.zzx.matchlens.observer;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 复盘记录观察者（观察者模式，优先级 4）。
 * <p>
 * 监听比赛事件，为每场比赛维护一个事件日志列表，
 * 供赛后复盘报告生成时使用。使用 {@code @Order(4)} 确保在其他观察者
 * （比分板、统计、风险预警）之后执行。
 * </p>
 */
@Component
@Order(4)
public class ReportObserver implements MatchObserver {

    /** 按比赛 ID 分组的事件日志表 */
    private final Map<String, List<MatchEvent>> matchEventLog = new HashMap<>();

    @Override
    public void onEventAdded(Match match, MatchEvent event) {
        matchEventLog
                .computeIfAbsent(match.getMatchId(), k -> new ArrayList<>())
                .add(event);

        System.out.printf("[复盘记录] 记录事件: %s (比赛: %s)%n",
                event.getEventType(), match.getMatchName());
    }

    /**
     * 获取指定比赛的事件日志。
     *
     * @param matchId 比赛 ID
     * @return 事件列表，若无记录则返回空列表
     */
    public List<MatchEvent> getEventLog(String matchId) {
        return matchEventLog.getOrDefault(matchId, List.of());
    }
}
