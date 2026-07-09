package com.zzx.matchlens.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zzx.matchlens.common.MatchStatus;
import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.common.SportType;
import com.zzx.matchlens.state.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 比赛实体类，代表一场体育赛事的完整信息。
 * 包含比赛名称、运动类型、参赛队伍、比赛状态及关联的事件和统计数据。
 * 采用状态模式（MatchState）管理比赛生命周期（未开始→进行中→暂停→结束），
 * 通过状态转换控制比赛流程中各操作的合法性校验。
 * 对应数据库表 t_match。
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("t_match")
public class Match {

    /** 比赛唯一标识 */
    @TableId
    private String matchId;
    /** 比赛名称 */
    private String matchName;
    /** 运动类型（如篮球、足球等） */
    private SportType sportType;
    /** 比赛状态（未开始/进行中/暂停/结束） */
    private MatchStatus status;
    /** 主队ID */
    private String homeTeamId;
    /** 客队ID */
    private String awayTeamId;
    /** 比赛创建时间 */
    private LocalDateTime createTime;
    /** 比赛信息最后更新时间 */
    private LocalDateTime updateTime;

    /** 比赛状态对象（状态模式，不持久化） */
    @TableField(exist = false)
    private transient MatchState state = new NotStartedState();

    /** 主队信息（不持久化，通过关联查询加载） */
    @TableField(exist = false)
    @JsonIgnore
    private Team homeTeam;

    /** 客队信息（不持久化，通过关联查询加载） */
    @TableField(exist = false)
    @JsonIgnore
    private Team awayTeam;

    /** 比赛事件列表（不持久化，通过关联查询加载） */
    @TableField(exist = false)
    private List<MatchEvent> events = new ArrayList<>();

    /** 比赛统计数据（不持久化，通过事件重放计算得出） */
    @TableField(exist = false)
    private MatchStatistics statistics = new MatchStatistics();

    public Match(String matchId, String matchName, SportType sportType) {
        this.matchId = matchId;
        this.matchName = matchName;
        this.sportType = sportType;
        this.status = MatchStatus.NOT_STARTED;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public void addEvent(MatchEvent event) {
        events.add(event);
    }

    public boolean removeEvent(String eventId) {
        return events.removeIf(e -> e.getEventId().equals(eventId));
    }

    public void clearPlayerStatistics() {
        if (homeTeam != null && homeTeam.getPlayers() != null) {
            homeTeam.getPlayers().forEach(p -> p.getStatistics().clear());
        }
        if (awayTeam != null && awayTeam.getPlayers() != null) {
            awayTeam.getPlayers().forEach(p -> p.getStatistics().clear());
        }
    }

    public Result<String> start() {
        return state.start(this);
    }

    public Result<String> pause() {
        return state.pause(this);
    }

    public Result<String> resume() {
        return state.resume(this);
    }

    public Result<String> finish() {
        return state.finish(this);
    }

    public Result<String> canAddEvent() {
        return state.addEvent(this);
    }

    public Result<String> canModifyTeams() {
        return state.modifyTeams(this);
    }

    public Result<String> canViewReport() {
        return state.viewReport(this);
    }

    public void syncState() {
        if (this.status == null) {
            this.state = new NotStartedState();
            return;
        }
        this.state = switch (status) {
            case NOT_STARTED -> new NotStartedState();
            case RUNNING -> new RunningState();
            case PAUSED -> new PausedState();
            case FINISHED -> new FinishedState();
        };
    }

    @JsonIgnore
    public MatchState getState() { return state; }

    @JsonProperty("teamA")
    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
        if (homeTeam != null) this.homeTeamId = homeTeam.getTeamId();
    }

    @JsonProperty("teamB")
    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
        if (awayTeam != null) this.awayTeamId = awayTeam.getTeamId();
    }

    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) {
        this.status = status;
        syncState();
    }
}
