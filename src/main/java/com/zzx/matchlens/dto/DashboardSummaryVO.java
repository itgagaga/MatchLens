package com.zzx.matchlens.dto;

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

    public long getMatchCount() { return matchCount; }
    public void setMatchCount(long matchCount) { this.matchCount = matchCount; }
    public long getTeamCount() { return teamCount; }
    public void setTeamCount(long teamCount) { this.teamCount = teamCount; }
    public long getPlayerCount() { return playerCount; }
    public void setPlayerCount(long playerCount) { this.playerCount = playerCount; }
    public long getEventCount() { return eventCount; }
    public void setEventCount(long eventCount) { this.eventCount = eventCount; }
    public long getFinishedMatchCount() { return finishedMatchCount; }
    public void setFinishedMatchCount(long finishedMatchCount) { this.finishedMatchCount = finishedMatchCount; }
    public long getRunningMatchCount() { return runningMatchCount; }
    public void setRunningMatchCount(long runningMatchCount) { this.runningMatchCount = runningMatchCount; }
    public long getAiCallCount() { return aiCallCount; }
    public void setAiCallCount(long aiCallCount) { this.aiCallCount = aiCallCount; }
    public long getAiSuccessCount() { return aiSuccessCount; }
    public void setAiSuccessCount(long aiSuccessCount) { this.aiSuccessCount = aiSuccessCount; }
    public long getAiFailCount() { return aiFailCount; }
    public void setAiFailCount(long aiFailCount) { this.aiFailCount = aiFailCount; }
    public double getAiSuccessRate() { return aiSuccessRate; }
    public void setAiSuccessRate(double aiSuccessRate) { this.aiSuccessRate = aiSuccessRate; }
    public long getBasketballMatchCount() { return basketballMatchCount; }
    public void setBasketballMatchCount(long basketballMatchCount) { this.basketballMatchCount = basketballMatchCount; }
    public long getFootballMatchCount() { return footballMatchCount; }
    public void setFootballMatchCount(long footballMatchCount) { this.footballMatchCount = footballMatchCount; }
    public long getVolleyballMatchCount() { return volleyballMatchCount; }
    public void setVolleyballMatchCount(long volleyballMatchCount) { this.volleyballMatchCount = volleyballMatchCount; }
    public long getGeneralMatchCount() { return generalMatchCount; }
    public void setGeneralMatchCount(long generalMatchCount) { this.generalMatchCount = generalMatchCount; }
}
