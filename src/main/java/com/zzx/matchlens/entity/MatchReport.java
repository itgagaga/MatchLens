package com.zzx.matchlens.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 比赛报告实体类，存储由 AI 生成的比赛分析报告。
 * 包含报告类型、标题、正文内容及生成者信息，关联到具体比赛，
 * 用于比赛结束后为用户提供智能化的赛事分析总结。
 * 对应数据库表 t_match_report。
 */
@Data
@NoArgsConstructor
@TableName("t_match_report")
public class MatchReport {

    /** 报告唯一标识 */
    @TableId
    private String reportId;
    /** 关联的比赛ID */
    private String matchId;
    /** 报告类型 */
    private String reportType;
    /** 报告标题 */
    private String title;
    /** 报告正文内容 */
    private String content;
    /** 报告生成者（如 AI 模型名称） */
    private String generatedBy;
    /** 报告创建时间 */
    private LocalDateTime createTime;
}
