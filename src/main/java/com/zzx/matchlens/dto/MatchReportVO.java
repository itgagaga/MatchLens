package com.zzx.matchlens.dto;

import java.time.LocalDateTime;

public class MatchReportVO {

    private String reportId;
    private String matchId;
    private String reportType;
    private String title;
    private String content;
    private String generatedBy;
    private LocalDateTime createTime;

    public MatchReportVO() {}

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
