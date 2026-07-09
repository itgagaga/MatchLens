package com.zzx.matchlens.chain;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;

/**
 * 比赛状态校验处理器（责任链第二环）。
 * <p>
 * 委托给 {@link com.zzx.matchlens.entity.Match#canAddEvent()} 方法，
 * 校验当前比赛状态是否允许添加新事件（例如已结束的赛事不允许再录入事件）。
 * </p>
 */
public class StateCheckHandler extends EventCheckHandler {

    /**
     * 校验比赛当前状态是否允许添加事件。
     *
     * @param match 比赛实体
     * @param event 待校验的比赛事件
     * @return 校验结果
     */
    @Override
    protected Result<String> check(Match match, MatchEvent event) {
        return match.canAddEvent();
    }
}
