package com.zzx.matchlens.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 球员实体类，代表一名参赛球员的基本信息。
 * 包含球员姓名、所属球队、号码、位置、年龄、身高、体重等，
 * 同时持有比赛进行时的实时统计数据（内存中维护，不持久化）。
 * 对应数据库表 t_player。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_player")
public class Player {

    /** 球员唯一标识 */
    @TableId
    private String playerId;
    /** 球员姓名 */
    private String playerName;
    /** 所属球队ID */
    private String teamId;
    /** 球衣号码 */
    private int number;
    /** 场上位置 */
    private String position;
    /** 年龄 */
    private int age;
    /** 身高（cm） */
    private int height;
    /** 体重（kg） */
    private int weight;

    /** 实时统计数据，key为统计项名称，value为统计值（不持久化，仅内存中维护） */
    @TableField(exist = false)
    private Map<String, Integer> statistics = new HashMap<>();

    public Player(String playerId, String playerName, String teamId, int number) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.teamId = teamId;
        this.number = number;
    }

    public Player(String playerId, String playerName, String teamId, int number,
                  String position, int age, int height, int weight) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.teamId = teamId;
        this.number = number;
        this.position = position;
        this.age = age;
        this.height = height;
        this.weight = weight;
    }

    public void addStat(String key, int value) {
        statistics.merge(key, value, Integer::sum);
    }

    public int getStat(String key) {
        return statistics.getOrDefault(key, 0);
    }
}
