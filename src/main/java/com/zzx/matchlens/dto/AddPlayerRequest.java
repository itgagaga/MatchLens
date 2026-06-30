package com.zzx.matchlens.dto;

public class AddPlayerRequest {
    private boolean teamA;
    private String playerName;
    private int number;

    public boolean isTeamA() { return teamA; }
    public void setTeamA(boolean teamA) { this.teamA = teamA; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
}
