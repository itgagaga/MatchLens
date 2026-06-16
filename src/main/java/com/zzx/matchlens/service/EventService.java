package com.zzx.matchlens.service;

import com.zzx.matchlens.chain.EventCheckChain;
import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;
import com.zzx.matchlens.entity.Player;
import com.zzx.matchlens.observer.MatchEventPublisher;
import com.zzx.matchlens.repository.MatchRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EventService {

    private final MatchRepository matchRepository;
    private final EventCheckChain eventCheckChain;
    private final MatchEventPublisher publisher;

    public EventService(MatchRepository matchRepository,
                        EventCheckChain eventCheckChain,
                        MatchEventPublisher publisher) {
        this.matchRepository = matchRepository;
        this.eventCheckChain = eventCheckChain;
        this.publisher = publisher;
    }

    public Result<String> recordEvent(String matchId, String teamId, String playerId,
                                    EventType eventType, int scoreValue, String description) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("比赛不存在: " + matchId));

        MatchEvent event = new MatchEvent(
                UUID.randomUUID().toString(), matchId, teamId, playerId,
                eventType, scoreValue, description);

        // 责任链校验
        Result<String> checkResult = eventCheckChain.execute(match, event);
        if (!checkResult.isSuccess()) {
            return Result.fail(checkResult.getMessage());
        }

        // 保存事件
        match.addEvent(event);

        // 更新球员统计
        Player player = findPlayer(match, playerId);
        if (player != null) {
            String statKey = eventType.name();
            player.addStat(statKey, scoreValue > 0 ? scoreValue : 1);
        }

        // 通知观察者
        publisher.publish(match, event);

        matchRepository.save(match);
        return Result.ok("事件录入成功");
    }

    private Player findPlayer(Match match, String playerId) {
        Player p = match.getHomeTeam().findPlayer(playerId);
        if (p != null) return p;
        return match.getAwayTeam().findPlayer(playerId);
    }
}
