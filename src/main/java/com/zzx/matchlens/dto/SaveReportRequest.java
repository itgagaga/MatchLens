package com.zzx.matchlens.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SaveReportRequest {

    private String reportType;
    private String title;
    private String content;
    private String generatedBy;
}
