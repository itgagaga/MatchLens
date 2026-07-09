package com.zzx.matchlens.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 球员统计数据实体类，持久化存储球员在每场比赛中的各项统计。
 * 每条记录对应一名球员在一场比赛中的某一项统计数据，
 * 采用KV结构（statKey/statValue）以支持不同运动类型的多样化统计项。
 * 对应数据库表 t_player_statistics。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_player_statistics")
public class PlayerStatistics {

    /** 自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关联的球员ID */
    private String playerId;
    /** 关联的比赛ID */
    private String matchId;
    /** 统计项名称（如 SCORE、FOUL 等） */
    private String statKey;
    /** 统计项的值 */
    private int statValue;

    public PlayerStatistics(String playerId, String matchId, String statKey, int statValue) {
        this.playerId = playerId;
        this.matchId = matchId;
        this.statKey = statKey;
        this.statValue = statValue;
    }
}
