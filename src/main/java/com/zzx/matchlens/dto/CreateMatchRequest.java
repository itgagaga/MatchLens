package com.zzx.matchlens.dto;

import com.zzx.matchlens.common.SportType;
import lombok.Data;

@Data
public class CreateMatchRequest {
    private String matchName;
    private SportType sportType;
}
