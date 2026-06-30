package com.zzx.matchlens.dto;

public class SaveReportRequest {

    private String reportType;
    private String title;
    private String content;
    private String generatedBy;

    public SaveReportRequest() {}

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
}
