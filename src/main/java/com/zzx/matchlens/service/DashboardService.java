package com.zzx.matchlens.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzx.matchlens.common.MatchStatus;
import com.zzx.matchlens.common.SportType;
import com.zzx.matchlens.dto.DashboardSummaryVO;
import com.zzx.matchlens.entity.AiCallLog;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.mapper.*;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final MatchMapper matchMapper;
    private final TeamMapper teamMapper;
    private final PlayerMapper playerMapper;
    private final MatchEventMapper matchEventMapper;
    private final AiCallLogMapper aiCallLogMapper;

    public DashboardService(MatchMapper matchMapper,
                            TeamMapper teamMapper,
                            PlayerMapper playerMapper,
                            MatchEventMapper matchEventMapper,
                            AiCallLogMapper aiCallLogMapper) {
        this.matchMapper = matchMapper;
        this.teamMapper = teamMapper;
        this.playerMapper = playerMapper;
        this.matchEventMapper = matchEventMapper;
        this.aiCallLogMapper = aiCallLogMapper;
    }

    public DashboardSummaryVO getSummary() {
        DashboardSummaryVO vo = new DashboardSummaryVO();

        // 基础统计
        vo.setMatchCount(matchMapper.selectCount(null));
        vo.setTeamCount(teamMapper.selectCount(null));
        vo.setPlayerCount(playerMapper.selectCount(null));
        vo.setEventCount(matchEventMapper.selectCount(null));

        // 比赛状态统计
        vo.setFinishedMatchCount(matchMapper.selectCount(
                new LambdaQueryWrapper<Match>().eq(Match::getStatus, MatchStatus.FINISHED)));
        vo.setRunningMatchCount(matchMapper.selectCount(
                new LambdaQueryWrapper<Match>().eq(Match::getStatus, MatchStatus.RUNNING)));

        // 赛事类型分布
        vo.setBasketballMatchCount(matchMapper.selectCount(
                new LambdaQueryWrapper<Match>().eq(Match::getSportType, SportType.BASKETBALL)));
        vo.setFootballMatchCount(matchMapper.selectCount(
                new LambdaQueryWrapper<Match>().eq(Match::getSportType, SportType.FOOTBALL)));
        vo.setVolleyballMatchCount(matchMapper.selectCount(
                new LambdaQueryWrapper<Match>().eq(Match::getSportType, SportType.VOLLEYBALL)));
        vo.setGeneralMatchCount(matchMapper.selectCount(
                new LambdaQueryWrapper<Match>().eq(Match::getSportType, SportType.GENERAL)));

        // AI 调用统计
        long totalAi = aiCallLogMapper.selectCount(null);
        long successAi = aiCallLogMapper.selectCount(
                new LambdaQueryWrapper<AiCallLog>().eq(AiCallLog::getSuccess, 1));
        long failAi = aiCallLogMapper.selectCount(
                new LambdaQueryWrapper<AiCallLog>().eq(AiCallLog::getSuccess, 0));

        vo.setAiCallCount(totalAi);
        vo.setAiSuccessCount(successAi);
        vo.setAiFailCount(failAi);
        vo.setAiSuccessRate(totalAi > 0 ? Math.round(successAi * 1000.0 / totalAi) / 10.0 : 0);

        return vo;
    }
}
