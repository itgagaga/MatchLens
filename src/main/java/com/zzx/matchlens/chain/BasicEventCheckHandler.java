package com.zzx.matchlens.chain;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;

/**
 * 基础事件校验处理器（责任链第一环）。
 * <p>
 * 执行最基本的非空校验：检查比赛对象是否存在、队伍是否已设置、
 * 事件数据及事件类型是否为空。作为责任链的起始节点，
 * 拦截明显无效的事件请求。
 * </p>
 */
public class BasicEventCheckHandler extends EventCheckHandler {

    /**
     * 执行基础非空校验。
     *
     * @param match 比赛实体
     * @param event 待校验的比赛事件
     * @return 校验结果，失败时包含错误描述
     */
    @Override
    protected Result<String> check(Match match, MatchEvent event) {
        if (match == null) {
            return Result.fail("比赛不存在");
        }
        if (match.getHomeTeam() == null || match.getAwayTeam() == null) {
            return Result.fail("比赛尚未设置队伍");
        }
        if (event == null) {
            return Result.fail("事件数据为空");
        }
        if (event.getEventType() == null) {
            return Result.fail("事件类型不能为空");
        }
        return Result.ok("基础校验通过");
    }
}
