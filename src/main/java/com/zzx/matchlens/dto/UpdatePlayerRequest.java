package com.zzx.matchlens.dto;

import lombok.Data;

@Data
public class UpdatePlayerRequest {
    private String playerName;
    private int number;
    private String position;
    private int age;
    private int height;
    private int weight;
}
