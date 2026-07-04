package com.zzx.matchlens.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zzx.matchlens.entity.QaMessage;
import com.zzx.matchlens.mapper.QaMessageMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class QaMessageService {

    private final QaMessageMapper qaMessageMapper;

    public QaMessageService(QaMessageMapper qaMessageMapper) {
        this.qaMessageMapper = qaMessageMapper;
    }

    /**
     * 保存一条问答消息（user 提问或 ai 回答）
     */
    public QaMessage saveMessage(String matchId, Long userId, String role, String content) {
        QaMessage msg = new QaMessage();
        msg.setMessageId(UUID.randomUUID().toString());
        msg.setMatchId(matchId);
        msg.setUserId(userId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setCreateTime(LocalDateTime.now());
        qaMessageMapper.insert(msg);
        return msg;
    }

    /**
     * 查询某场比赛某用户的所有问答历史（按时间升序）
     */
    public List<QaMessage> getHistory(String matchId, Long userId) {
        QueryWrapper<QaMessage> qw = new QueryWrapper<>();
        qw.eq("match_id", matchId)
          .eq("user_id", userId)
          .orderByAsc("create_time");
        return qaMessageMapper.selectList(qw);
    }

    /**
     * 删除某场比赛某用户的所有问答历史
     */
    public void clearHistory(String matchId, Long userId) {
        QueryWrapper<QaMessage> qw = new QueryWrapper<>();
        qw.eq("match_id", matchId).eq("user_id", userId);
        qaMessageMapper.delete(qw);
    }
}
