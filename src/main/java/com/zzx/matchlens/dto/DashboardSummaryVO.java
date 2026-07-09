package com.zzx.matchlens.dto;

import lombok.Data;

@Data
public class DashboardSummaryVO {
    private long matchCount;
    private long teamCount;
    private long playerCount;
    private long eventCount;
    private long finishedMatchCount;
    private long runningMatchCount;
    private long aiCallCount;
    private long aiSuccessCount;
    private long aiFailCount;
    private double aiSuccessRate;
    private long basketballMatchCount;
    private long footballMatchCount;
    private long volleyballMatchCount;
    private long generalMatchCount;
}
