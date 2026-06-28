package com.zzx.matchlens.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzx.matchlens.chain.EventCheckChain;
import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.dto.UpdateEventRequest;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;
import com.zzx.matchlens.mapper.MatchEventMapper;
import com.zzx.matchlens.observer.MatchEventPublisher;
import com.zzx.matchlens.repository.MatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    private final MatchRepository matchRepository;
    private final MatchEventMapper matchEventMapper;
    private final EventCheckChain eventCheckChain;
    private final MatchEventPublisher publisher;
    private final StatisticsRebuildService rebuildService;

    public EventService(MatchRepository matchRepository,
                        MatchEventMapper matchEventMapper,
                        EventCheckChain eventCheckChain,
                        MatchEventPublisher publisher,
                        StatisticsRebuildService rebuildService) {
        this.matchRepository = matchRepository;
        this.matchEventMapper = matchEventMapper;
        this.eventCheckChain = eventCheckChain;
        this.publisher = publisher;
        this.rebuildService = rebuildService;
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

        // 球员统计更新由 StatisticsObserver 统一负责，避免重复计数

        // 通知观察者
        publisher.publish(match, event);

        matchRepository.save(match);
        return Result.ok("事件录入成功");
    }

    /**
     * 查询某场比赛的事件时间线，按 event_time 升序排列
     */
    public List<MatchEvent> getMatchEvents(String matchId) {
        return matchEventMapper.selectList(
                new LambdaQueryWrapper<MatchEvent>()
                        .eq(MatchEvent::getMatchId, matchId)
                        .orderByAsc(MatchEvent::getEventTime));
    }

    /**
     * 按条件筛选事件
     */
    public List<MatchEvent> getFilteredEvents(String matchId, String teamId,
                                               String playerId, EventType eventType) {
        LambdaQueryWrapper<MatchEvent> wrapper = new LambdaQueryWrapper<>();
        if (matchId != null && !matchId.isEmpty()) {
            wrapper.eq(MatchEvent::getMatchId, matchId);
        }
        if (teamId != null && !teamId.isEmpty()) {
            wrapper.eq(MatchEvent::getTeamId, teamId);
        }
        if (playerId != null && !playerId.isEmpty()) {
            wrapper.eq(MatchEvent::getPlayerId, playerId);
        }
        if (eventType != null) {
            wrapper.eq(MatchEvent::getEventType, eventType);
        }
        wrapper.orderByAsc(MatchEvent::getEventTime);
        return matchEventMapper.selectList(wrapper);
    }

    /**
     * 查询单个事件详情
     */
    public MatchEvent getEvent(String eventId) {
        return matchEventMapper.selectById(eventId);
    }

    /**
     * 修改事件信息，经过责任链校验后触发统计重算
     */
    public Result<String> updateEvent(String eventId, UpdateEventRequest request) {
        MatchEvent existing = matchEventMapper.selectById(eventId);
        if (existing == null) {
            return Result.fail("事件不存在: " + eventId);
        }

        Match match = matchRepository.findById(existing.getMatchId())
                .orElseThrow(() -> new RuntimeException("比赛不存在: " + existing.getMatchId()));

        // 构建更新后的事件用于校验
        MatchEvent updated = new MatchEvent(
                eventId, existing.getMatchId(),
                request.getTeamId(), request.getPlayerId(),
                request.getEventType(), request.getScoreValue(),
                request.getDescription());
        if (request.getEventTime() != null) {
            updated.setEventTime(request.getEventTime());
        } else {
            updated.setEventTime(existing.getEventTime());
        }

        // 责任链校验
        Result<String> checkResult = eventCheckChain.execute(match, updated);
        if (!checkResult.isSuccess()) {
            return Result.fail(checkResult.getMessage());
        }

        // 更新事件
        matchEventMapper.updateById(updated);

        // 全量重算统计
        rebuildService.rebuildStatistics(existing.getMatchId());

        return Result.ok("事件修改成功");
    }

    /**
     * 删除事件后触发统计重算
     */
    public Result<String> deleteEvent(String eventId) {
        MatchEvent existing = matchEventMapper.selectById(eventId);
        if (existing == null) {
            return Result.fail("事件不存在: " + eventId);
        }

        String matchId = existing.getMatchId();
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("比赛不存在: " + matchId));

        // 检查比赛状态：已结束的比赛不允许删除事件
        Result<String> stateCheck = match.canAddEvent();
        if (!stateCheck.isSuccess()) {
            return Result.fail("当前比赛状态不允许删除事件: " + stateCheck.getMessage());
        }

        // 删除事件
        matchEventMapper.deleteById(eventId);

        // 全量重算统计
        rebuildService.rebuildStatistics(matchId);

        return Result.ok("事件删除成功");
    }
}
