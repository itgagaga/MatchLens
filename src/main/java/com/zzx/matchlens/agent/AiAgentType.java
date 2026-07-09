package com.zzx.matchlens.agent;

/**
 * AI 智能体类型枚举。
 * <p>
 * 定义了系统中所有 AI Agent 的功能类型，每种类型对应一种智能分析场景，
 * 用于区分请求来源、日志记录以及降级策略的选择。
 * </p>
 */
public enum AiAgentType {

    /** 实时态势分析：对进行中的比赛进行实时数据分析 */
    SITUATION_ANALYSIS("实时态势分析"),

    /** 赛后智能复盘：比赛结束后生成全面的复盘报告 */
    REVIEW_REPORT("赛后智能复盘"),

    /** 智能比赛推荐：根据用户偏好推荐感兴趣的比赛 */
    MATCH_RECOMMENDATION("智能比赛推荐"),

    /** 赛事智能问答：回答用户关于赛事的提问 */
    MATCH_QA("赛事智能问答");

    /** 类型的中文描述 */
    private final String description;

    AiAgentType(String description) {
        this.description = description;
    }

    /**
     * 获取该 Agent 类型的中文描述。
     *
     * @return 中文描述字符串
     */
    public String getDescription() {
        return description;
    }
}
