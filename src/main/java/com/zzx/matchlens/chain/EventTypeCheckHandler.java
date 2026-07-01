package com.zzx.matchlens.chain;

import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.common.SportType;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;

import java.util.Set;

public class EventTypeCheckHandler extends EventCheckHandler {

    private static final Set<EventType> BASKETBALL_EVENTS = Set.of(
            EventType.SCORE, EventType.FOUL, EventType.ASSIST,
            EventType.REBOUND, EventType.STEAL, EventType.TURNOVER,
            EventType.TIMEOUT, EventType.SUBSTITUTION
    );

    private static final Set<EventType> FOOTBALL_EVENTS = Set.of(
            EventType.SCORE, EventType.FOUL, EventType.YELLOW_CARD,
            EventType.RED_CARD, EventType.SUBSTITUTION, EventType.TIMEOUT
    );

    private static final Set<EventType> VOLLEYBALL_EVENTS = Set.of(
            EventType.SCORE, EventType.BLOCK, EventType.SERVE_ACE,
            EventType.ERROR, EventType.TIMEOUT, EventType.SUBSTITUTION,
            EventType.FOUL
    );

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
