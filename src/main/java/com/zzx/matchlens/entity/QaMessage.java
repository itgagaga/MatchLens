package com.zzx.matchlens.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI问答消息实体类，记录用户与AI赛事问答助手之间的对话消息。
 * 每条消息关联到具体比赛和用户，区分用户角色（user/assistant），
 * 用于持久化对话历史，支持会话上下文恢复。
 * 对应数据库表 t_qa_message。
 */
@Data
@NoArgsConstructor
@TableName("t_qa_message")
public class QaMessage {

    /** 消息唯一标识 */
    @TableId
    private String messageId;
    /** 关联的比赛ID */
    private String matchId;
    /** 发送用户ID */
    private Long userId;
    /** 消息角色（user-用户提问 / assistant-AI回复） */
    private String role;
    /** 消息内容 */
    private String content;
    /** 消息创建时间 */
    private LocalDateTime createTime;
}
