package com.zzx.matchlens.dto;

public class AddPlayerRequest {
    private boolean home;
    private String playerName;
    private int number;

    public boolean isHome() { return home; }
    public void setHome(boolean home) { this.home = home; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
}
