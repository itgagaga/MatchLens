package com.zzx.matchlens.chain;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;

/**
 * 事件校验处理器抽象基类（责任链模式）。
 * <p>
 * 定义了责任链的基本结构：每个处理器持有下一个处理器的引用，
 * 当前处理器执行校验通过后，将请求传递给下一个处理器；
 * 若校验失败则立即返回错误，不再向下传递。
 * </p>
 */
public abstract class EventCheckHandler {

    /** 责任链中的下一个处理器 */
    private EventCheckHandler next;

    /**
     * 设置责任链中的下一个处理器。
     *
     * @param next 下一个处理器
     * @return 传入的下一个处理器，便于链式调用
     */
    public EventCheckHandler setNext(EventCheckHandler next) {
        this.next = next;
        return next;
    }

    /**
     * 处理请求：先执行当前处理器的校验，通过后递归调用下一个处理器。
     *
     * @param match 比赛实体
     * @param event 待校验的比赛事件
     * @return 校验结果
     */
    public Result<String> handle(Match match, MatchEvent event) {
        Result<String> result = check(match, event);
        if (!result.isSuccess()) {
            return result;
        }
        if (next != null) {
            return next.handle(match, event);
        }
        return Result.ok("校验通过");
    }

    /**
     * 具体的校验逻辑，由子类实现。
     *
     * @param match 比赛实体
     * @param event 待校验的比赛事件
     * @return 校验结果
     */
    protected abstract Result<String> check(Match match, MatchEvent event);
}
