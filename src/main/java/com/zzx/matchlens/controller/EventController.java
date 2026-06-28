package com.zzx.matchlens.controller;

import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.dto.RecordEventRequest;
import com.zzx.matchlens.dto.UpdateEventRequest;
import com.zzx.matchlens.entity.MatchEvent;
import com.zzx.matchlens.service.EventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * 录入事件（原有接口）
     */
    @PostMapping("/matches/{matchId}/events")
    public Result<String> recordEvent(@PathVariable String matchId, @RequestBody RecordEventRequest request) {
        return eventService.recordEvent(
                matchId,
                request.getTeamId(),
                request.getPlayerId(),
                request.getEventType(),
                request.getScoreValue(),
                request.getDescription()
        );
    }

    /**
     * 查询某场比赛的事件时间线，按 event_time 升序排列
     */
    @GetMapping("/matches/{matchId}/events")
    public Result<List<MatchEvent>> getMatchEvents(@PathVariable String matchId) {
        List<MatchEvent> events = eventService.getMatchEvents(matchId);
        return Result.ok(events);
    }

    /**
     * 按条件筛选事件
     */
    @GetMapping("/events")
    public Result<List<MatchEvent>> getFilteredEvents(
            @RequestParam(required = false) String matchId,
            @RequestParam(required = false) String teamId,
            @RequestParam(required = false) String playerId,
            @RequestParam(required = false) EventType eventType) {
        List<MatchEvent> events = eventService.getFilteredEvents(matchId, teamId, playerId, eventType);
        return Result.ok(events);
    }

    /**
     * 查询单个事件详情
     */
    @GetMapping("/events/{eventId}")
    public Result<MatchEvent> getEvent(@PathVariable String eventId) {
        MatchEvent event = eventService.getEvent(eventId);
        if (event == null) {
            return Result.fail("事件不存在");
        }
        return Result.ok(event);
    }

    /**
     * 修改事件信息
     */
    @PutMapping("/events/{eventId}")
    public Result<String> updateEvent(@PathVariable String eventId, @RequestBody UpdateEventRequest request) {
        return eventService.updateEvent(eventId, request);
    }

    /**
     * 删除事件
     */
    @DeleteMapping("/events/{eventId}")
    public Result<String> deleteEvent(@PathVariable String eventId) {
        return eventService.deleteEvent(eventId);
    }
}
