package com.zzx.matchlens.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * AI调用日志实体类，记录每次AI Agent调用的详细信息。
 * 包含调用的Agent类型、关联比赛、提示词、响应内容、是否成功、错误信息、响应耗时等，
 * 用于AI调用的审计追踪和性能监控。
 * 对应数据库表 t_ai_call_log。
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("t_ai_call_log")
public class AiCallLog {

    /** 调用日志唯一标识 */
    @TableId
    private String id;
    /** 调用的 Agent 类型 */
    private String agentType;
    /** 关联的比赛ID */
    private String matchId;
    /** 发送给 AI 的提示词 */
    private String prompt;
    /** AI 返回的响应内容 */
    private String response;
    /** 调用是否成功（1-成功，0-失败） */
    private Integer success;
    /** 失败时的错误信息 */
    private String errorMessage;
    /** 响应耗时（毫秒） */
    private Long responseTimeMs;
    /** 调用时间 */
    private LocalDateTime callTime;

    @JsonProperty("createTime")
    public LocalDateTime getCreateTime() { return callTime; }

    @JsonProperty("duration")
    public Long getDuration() { return responseTimeMs; }
}
