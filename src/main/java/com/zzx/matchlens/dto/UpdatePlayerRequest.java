package com.zzx.matchlens.dto;

public class UpdatePlayerRequest {
    private String playerName;
    private int number;
    private String position;
    private int age;
    private int height;
    private int weight;

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
}
