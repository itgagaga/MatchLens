package com.zzx.matchlens.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzx.matchlens.agent.AiAgentResponse;
import com.zzx.matchlens.entity.AiCallLog;
import com.zzx.matchlens.mapper.AiCallLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AiCallLogService {

    private final AiCallLogMapper aiCallLogMapper;

    public AiCallLogService(AiCallLogMapper aiCallLogMapper) {
        this.aiCallLogMapper = aiCallLogMapper;
    }

    public void log(AiAgentResponse response, String prompt) {
        AiCallLog log = new AiCallLog();
        log.setId(UUID.randomUUID().toString());
        log.setAgentType(response.getAgentType() != null ? response.getAgentType().name() : "UNKNOWN");
        log.setMatchId(response.getMatchId());
        log.setPrompt(prompt);
        log.setSuccess(response.isSuccess() ? 1 : 0);
        log.setResponseTimeMs(response.getResponseTimeMs());
        log.setCallTime(LocalDateTime.now());

        if (response.isSuccess()) {
            log.setResponse(truncate(response.getContent(), 2000));
        } else {
            log.setErrorMessage(truncate(response.getErrorMessage(), 500));
        }

        aiCallLogMapper.insert(log);
    }

    public List<AiCallLog> queryLogs(String matchId, String agentType, Integer success, String keyword) {
        LambdaQueryWrapper<AiCallLog> wrapper = new LambdaQueryWrapper<>();
        if (matchId != null && !matchId.isEmpty()) {
            wrapper.eq(AiCallLog::getMatchId, matchId);
        }
        if (agentType != null && !agentType.isEmpty()) {
            wrapper.eq(AiCallLog::getAgentType, agentType);
        }
        if (success != null) {
            wrapper.eq(AiCallLog::getSuccess, success);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(AiCallLog::getPrompt, keyword)
                    .or().like(AiCallLog::getResponse, keyword)
                    .or().like(AiCallLog::getErrorMessage, keyword));
        }
        wrapper.orderByDesc(AiCallLog::getCallTime);
        return aiCallLogMapper.selectList(wrapper);
    }

    public AiCallLog getLog(String id) {
        return aiCallLogMapper.selectById(id);
    }

    public void deleteLog(String id) {
        AiCallLog log = aiCallLogMapper.selectById(id);
        if (log == null) {
            throw new RuntimeException("日志不存在: " + id);
        }
        aiCallLogMapper.deleteById(id);
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }
}
