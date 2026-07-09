package com.zzx.matchlens.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zzx.matchlens.common.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 比赛事件实体类，记录比赛过程中发生的每一个事件（如得分、犯规、换人等）。
 * 每个事件关联所属比赛、队伍和球员，包含事件类型、得分值、事件时间及描述信息，
 * 是比赛时间线和统计数据重建的核心数据源。
 * 对应数据库表 t_match_event。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_match_event")
public class MatchEvent {

    /** 事件唯一标识 */
    @TableId
    private String eventId;
    /** 所属比赛ID */
    private String matchId;
    /** 关联的球队ID */
    private String teamId;
    /** 关联的球员ID */
    private String playerId;
    /** 事件类型（如进球、犯规、换人等） */
    private EventType eventType;
    /** 得分值（仅得分事件有效） */
    private int scoreValue;
    /** 事件发生时间 */
    private LocalDateTime eventTime;
    /** 事件描述信息 */
    private String description;

    public MatchEvent(String eventId, String matchId, String teamId, String playerId,
                      EventType eventType, int scoreValue, String description) {
        this.eventId = eventId;
        this.matchId = matchId;
        this.teamId = teamId;
        this.playerId = playerId;
        this.eventType = eventType;
        this.scoreValue = scoreValue;
        this.description = description;
        this.eventTime = LocalDateTime.now();
    }
}
