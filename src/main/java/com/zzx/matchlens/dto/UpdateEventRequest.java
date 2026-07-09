package com.zzx.matchlens.dto;

import com.zzx.matchlens.common.EventType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateEventRequest {
    private String teamId;
    private String playerId;
    private EventType eventType;
    private int scoreValue;
    private LocalDateTime eventTime;
    private String description;
}
