package com.zzx.matchlens.chain;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;
import org.springframework.stereotype.Component;

/**
 * 事件校验责任链（建造者 + 责任链模式）。
 * <p>
 * 在构造方法中组装完整的校验链：
 * {@link BasicEventCheckHandler} → {@link StateCheckHandler} →
 * {@link PlayerCheckHandler} → {@link EventTypeCheckHandler} →
 * {@link ScoreCheckHandler}。
 * 外部只需调用 {@link #execute} 即可依次执行全部校验，
 * 任一环节失败则立即返回错误。
 * </p>
 */
@Component
public class EventCheckChain {

    /** 责任链的头节点 */
    private final EventCheckHandler head;

    /**
     * 构造方法，组装事件校验责任链。
     * <p>
     * 链顺序：基础校验 → 状态校验 → 球员校验 → 事件类型校验 → 分值校验。
     * </p>
     */
    public EventCheckChain() {
        EventCheckHandler basic = new BasicEventCheckHandler();
        EventCheckHandler state = new StateCheckHandler();
        EventCheckHandler player = new PlayerCheckHandler();
        EventCheckHandler eventType = new EventTypeCheckHandler();
        EventCheckHandler score = new ScoreCheckHandler();

        basic.setNext(state).setNext(player).setNext(eventType).setNext(score);
        this.head = basic;
    }

    /**
     * 执行事件校验责任链。
     *
     * @param match 比赛实体
     * @param event 待校验的比赛事件
     * @return 校验结果，所有环节通过则返回成功
     */
    public Result<String> execute(Match match, MatchEvent event) {
        return head.handle(match, event);
    }
}
