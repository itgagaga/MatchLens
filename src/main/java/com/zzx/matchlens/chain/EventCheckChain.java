package com.zzx.matchlens.chain;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;

public class EventCheckChain {

    private final EventCheckHandler head;

    public EventCheckChain() {
        EventCheckHandler basic = new BasicEventCheckHandler();
        EventCheckHandler state = new StateCheckHandler();
        EventCheckHandler player = new PlayerCheckHandler();
        EventCheckHandler eventType = new EventTypeCheckHandler();
        EventCheckHandler score = new ScoreCheckHandler();

        basic.setNext(state).setNext(player).setNext(eventType).setNext(score);
        this.head = basic;
    }

    public Result<String> execute(Match match, MatchEvent event) {
        return head.handle(match, event);
    }
}
