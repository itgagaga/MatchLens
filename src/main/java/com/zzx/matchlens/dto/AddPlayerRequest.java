package com.zzx.matchlens.dto;

import lombok.Data;

@Data
public class AddPlayerRequest {
    private boolean teamA;
    private String playerName;
    private int number;
}
