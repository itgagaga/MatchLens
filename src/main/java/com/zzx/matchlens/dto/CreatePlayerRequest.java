package com.zzx.matchlens.dto;

import lombok.Data;

@Data
public class CreatePlayerRequest {
    private String playerName;
    private String teamId;
    private int number;
    private String position;
    private int age;
    private int height;
    private int weight;
}
