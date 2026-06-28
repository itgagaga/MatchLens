package com.zzx.matchlens.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_player_statistics")
public class PlayerStatistics {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String playerId;
    private String matchId;
    private String statKey;
    private int statValue;

    public PlayerStatistics() {}

    public PlayerStatistics(String playerId, String matchId, String statKey, int statValue) {
        this.playerId = playerId;
        this.matchId = matchId;
        this.statKey = statKey;
        this.statValue = statValue;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
    public String getStatKey() { return statKey; }
    public void setStatKey(String statKey) { this.statKey = statKey; }
    public int getStatValue() { return statValue; }
    public void setStatValue(int statValue) { this.statValue = statValue; }
}
