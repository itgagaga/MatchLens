package com.zzx.matchlens.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MatchReportVO {

    private String reportId;
    private String matchId;
    private String reportType;
    private String title;
    private String content;
    private String generatedBy;
    private LocalDateTime createTime;
}
