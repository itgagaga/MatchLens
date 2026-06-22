package com.zzx.matchlens.observer;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReportObserver implements MatchObserver {

    private final Map<String, List<MatchEvent>> matchEventLog = new HashMap<>();

    @Override
    public void onEventAdded(Match match, MatchEvent event) {
        matchEventLog
                .computeIfAbsent(match.getMatchId(), k -> new ArrayList<>())
                .add(event);

        System.out.printf("[复盘记录] 记录事件: %s (比赛: %s)%n",
                event.getEventType(), match.getMatchName());
    }

    public List<MatchEvent> getEventLog(String matchId) {
        return matchEventLog.getOrDefault(matchId, List.of());
    }
}
