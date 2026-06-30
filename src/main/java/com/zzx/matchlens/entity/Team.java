package com.zzx.matchlens.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.ArrayList;
import java.util.List;

@TableName("t_team")
public class Team {

    @TableId
    private String teamId;
    private String teamName;
    private int score;
    private String city;
    private String coachName;

    @TableField(exist = false)
    private List<Player> players = new ArrayList<>();

    public Team() {}

    public Team(String teamId, String teamName) {
        this.teamId = teamId;
        this.teamName = teamName;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public Player findPlayer(String playerId) {
        return players.stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }

    public void addScore(int points) {
        this.score += points;
    }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCoachName() { return coachName; }
    public void setCoachName(String coachName) { this.coachName = coachName; }
}
