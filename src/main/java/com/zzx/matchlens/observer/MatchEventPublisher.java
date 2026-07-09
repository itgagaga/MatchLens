package com.zzx.matchlens.observer;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 比赛事件发布者（观察者模式）。
 * <p>
 * 维护观察者列表，当比赛事件发生时，遍历所有已注册的
 * {@link MatchObserver} 并通知它们处理新事件。
 * 支持动态订阅和取消订阅观察者。
 * </p>
 */
@Component
public class MatchEventPublisher {

    /** 已注册的观察者列表 */
    private final List<MatchObserver> observers = new ArrayList<>();

    /**
     * 构造方法，通过 Spring 自动注入所有 MatchObserver 实现。
     *
     * @param observers Spring 容器中所有 MatchObserver 类型的 Bean
     */
    public MatchEventPublisher(List<MatchObserver> observers) {
        this.observers.addAll(observers);
    }

    /**
     * 订阅观察者。
     *
     * @param observer 要添加的观察者
     */
    public void subscribe(MatchObserver observer) {
        observers.add(observer);
    }

    /**
     * 取消订阅观察者。
     *
     * @param observer 要移除的观察者
     */
    public void unsubscribe(MatchObserver observer) {
        observers.remove(observer);
    }

    /**
     * 发布事件通知，遍历所有观察者并触发其响应逻辑。
     *
     * @param match 发生事件的比赛
     * @param event 新添加的比赛事件
     */
    public void publish(Match match, MatchEvent event) {
        for (MatchObserver observer : observers) {
            observer.onEventAdded(match, event);
        }
    }
}
