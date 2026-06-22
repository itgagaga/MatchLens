package com.zzx.matchlens.dto;

import com.zzx.matchlens.common.SportType;

public class CreateMatchRequest {
    private String matchName;
    private SportType sportType;

    public String getMatchName() { return matchName; }
    public void setMatchName(String matchName) { this.matchName = matchName; }
    public SportType getSportType() { return sportType; }
    public void setSportType(SportType sportType) { this.sportType = sportType; }
}
