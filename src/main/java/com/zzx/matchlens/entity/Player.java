package com.zzx.matchlens.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.HashMap;
import java.util.Map;

@TableName("t_player")
public class Player {

    @TableId
    private String playerId;
    private String playerName;
    private String teamId;
    private int number;

    @TableField(exist = false)
    private Map<String, Integer> statistics = new HashMap<>();

    public Player() {}

    public Player(String playerId, String playerName, String teamId, int number) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.teamId = teamId;
        this.number = number;
    }

    public void addStat(String key, int value) {
        statistics.merge(key, value, Integer::sum);
    }

    public int getStat(String key) {
        return statistics.getOrDefault(key, 0);
    }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public Map<String, Integer> getStatistics() { return statistics; }
    public void setStatistics(Map<String, Integer> statistics) { this.statistics = statistics; }
}
