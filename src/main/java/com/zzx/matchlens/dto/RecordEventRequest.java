package com.zzx.matchlens.dto;

import com.zzx.matchlens.common.EventType;
import lombok.Data;

@Data
public class RecordEventRequest {
    private String teamId;
    private String playerId;
    private EventType eventType;
    private int scoreValue;
    private String description;
}
