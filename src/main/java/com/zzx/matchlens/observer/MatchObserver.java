package com.zzx.matchlens.observer;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;

/**
 * 比赛观察者接口（观察者模式）。
 * <p>
 * 定义观察者的统一契约：当比赛有新事件添加时，
 * 由 {@link MatchEventPublisher} 调用 {@link #onEventAdded} 方法进行响应。
 * 不同的观察者实现负责比分更新、统计刷新、风险预警、复盘记录等不同职责。
 * </p>
 */
public interface MatchObserver {

    /**
     * 当比赛事件被添加时触发。
     *
     * @param match 发生事件的比赛
     * @param event 新添加的比赛事件
     */
    void onEventAdded(Match match, MatchEvent event);
}
