package com.zzx.matchlens.dto;

public class SetTeamsRequest {
    private String homeName;
    private String awayName;

    public String getHomeName() { return homeName; }
    public void setHomeName(String homeName) { this.homeName = homeName; }
    public String getAwayName() { return awayName; }
    public void setAwayName(String awayName) { this.awayName = awayName; }
}
