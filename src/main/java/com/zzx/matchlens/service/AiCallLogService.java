package com.zzx.matchlens.service;

import com.zzx.matchlens.agent.AiAgentResponse;
import com.zzx.matchlens.entity.AiCallLog;
import com.zzx.matchlens.mapper.AiCallLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }
}
