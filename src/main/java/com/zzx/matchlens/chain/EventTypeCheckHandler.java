package com.zzx.matchlens.chain;

import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.common.SportType;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;

import java.util.Set;

/**
 * 事件类型校验处理器（责任链第四环）。
 * <p>
 * 根据赛事类型（篮球、足球、排球、通用）校验事件类型是否合法。
 * 每种运动只允许特定的事件类型，例如篮球允许得分/犯规/助攻等，
 * 足球允许得分/犯规/黄牌/红牌等。通用赛事允许所有事件类型。
 * </p>
 */
public class EventTypeCheckHandler extends EventCheckHandler {

    /** 篮球赛事允许的事件类型集合 */
    private static final Set<EventType> BASKETBALL_EVENTS = Set.of(
            EventType.SCORE, EventType.FOUL, EventType.ASSIST,
            EventType.REBOUND, EventType.STEAL, EventType.TURNOVER,
            EventType.TIMEOUT, EventType.SUBSTITUTION
    );

    /** 足球赛事允许的事件类型集合 */
    private static final Set<EventType> FOOTBALL_EVENTS = Set.of(
            EventType.SCORE, EventType.FOUL, EventType.YELLOW_CARD,
            EventType.RED_CARD, EventType.SUBSTITUTION, EventType.TIMEOUT
    );

    /** 排球赛事允许的事件类型集合 */
    private static final Set<EventType> VOLLEYBALL_EVENTS = Set.of(
            EventType.SCORE, EventType.BLOCK, EventType.SERVE_ACE,
            EventType.ERROR, EventType.TIMEOUT, EventType.SUBSTITUTION,
            EventType.FOUL
    );

    /**
     * 校验事件类型是否适用于当前赛事类型。
     *
     * @param match 比赛实体，用于获取赛事类型
     * @param event 待校验的比赛事件
     * @return 校验结果
     */
    @Override
    protected Result<String> check(Match match, MatchEvent event) {
        SportType sport = match.getSportType();
        EventType eventType = event.getEventType();

        Set<EventType> allowed = switch (sport) {
            case BASKETBALL -> BASKETBALL_EVENTS;
            case FOOTBALL -> FOOTBALL_EVENTS;
            case VOLLEYBALL -> VOLLEYBALL_EVENTS;
            case GENERAL -> Set.of(EventType.values());
        };

        if (!allowed.contains(eventType)) {
            return Result.fail("事件类型 " + eventType + " 不适用于 " + sport + " 赛事");
        }

        return Result.ok("事件类型校验通过");
    }
}
