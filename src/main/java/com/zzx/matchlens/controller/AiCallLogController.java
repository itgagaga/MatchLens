package com.zzx.matchlens.controller;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.AiCallLog;
import com.zzx.matchlens.service.AiCallLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-logs")
public class AiCallLogController {

    private final AiCallLogService aiCallLogService;

    public AiCallLogController(AiCallLogService aiCallLogService) {
        this.aiCallLogService = aiCallLogService;
    }

    @GetMapping
    public List<AiCallLog> queryLogs(
            @RequestParam(required = false) String matchId,
            @RequestParam(required = false) String agentType,
            @RequestParam(required = false) Integer success,
            @RequestParam(required = false) String keyword) {
        return aiCallLogService.queryLogs(matchId, agentType, success, keyword);
    }

    @GetMapping("/{id}")
    public AiCallLog getLog(@PathVariable String id) {
        AiCallLog log = aiCallLogService.getLog(id);
        if (log == null) {
            throw new RuntimeException("日志不存在: " + id);
        }
        return log;
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteLog(@PathVariable String id) {
        try {
            aiCallLogService.deleteLog(id);
            return Result.ok("日志删除成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
