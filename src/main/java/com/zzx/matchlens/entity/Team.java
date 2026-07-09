package com.zzx.matchlens.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 球队实体类，代表一支参赛球队的基本信息。
 * 包含球队名称、种子积分、所在城市、主教练等，
 * 同时持有球员列表（内存中维护，不持久化）。
 * 对应数据库表 t_team。
 */
@Data
@NoArgsConstructor
@TableName("t_team")
public class Team {

    /** 球队唯一标识 */
    @TableId
    private String teamId;
    /** 球队名称 */
    private String teamName;
    /** 种子积分（用于排名参考，非实时比分） */
    private int score;
    /** 所在城市 */
    private String city;
    /** 主教练姓名 */
    private String coachName;

    /** 球员列表（不持久化，通过关联查询加载） */
    @TableField(exist = false)
    private List<Player> players = new ArrayList<>();

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

    public void addScore(int points) {
        this.score += points;
    }
}
