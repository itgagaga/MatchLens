package com.zzx.matchlens.controller;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.dto.RecordEventRequest;
import com.zzx.matchlens.service.EventService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches/{matchId}/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
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
}
