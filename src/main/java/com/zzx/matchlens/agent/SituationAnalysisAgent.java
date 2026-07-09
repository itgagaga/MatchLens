package com.zzx.matchlens.agent;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.strategy.AnalysisStrategy;
import com.zzx.matchlens.strategy.AnalysisStrategyFactory;
import org.springframework.stereotype.Component;

/**
 * 实时态势分析智能体。
 * <p>
 * 对进行中的比赛进行实时态势分析。优先调用远程 AI 模型生成深度分析；
 * 若远程调用失败，则降级到本地策略模式（通过 {@link AnalysisStrategyFactory}）
 * 根据运动类型选择对应的分析策略生成本地分析结果。
 * </p>
 */
@Component
public class SituationAnalysisAgent implements AiAgent {

    private final DataCollectAgent dataCollectAgent;
    private final RemoteModelAgent remoteModelAgent;

    /**
     * 构造方法，注入所需的子 Agent 依赖。
     *
     * @param dataCollectAgent 数据采集 Agent，负责收集比赛结构化数据
     * @param remoteModelAgent 远程模型 Agent，负责调用远程 AI 服务
     */
    public SituationAnalysisAgent(DataCollectAgent dataCollectAgent,
                                   RemoteModelAgent remoteModelAgent) {
        this.dataCollectAgent = dataCollectAgent;
        this.remoteModelAgent = remoteModelAgent;
    }

    @Override
    public String execute(Match match) {
        return analyze(match);
    }

    /**
     * 执行实时态势分析。
     * <p>
     * 先尝试远程 AI 分析，失败时降级到本地策略分析。
     * </p>
     *
     * @param match 待分析的比赛实体
     * @return 态势分析报告文本
     */
    public String analyze(Match match) {
        String systemPrompt = buildSystemPrompt(match);
        String userPrompt = dataCollectAgent.collect(match);

        AiAgentResponse remoteResponse = remoteModelAgent.callWithPrompt(
                match, AiAgentType.SITUATION_ANALYSIS, systemPrompt, userPrompt);

        if (remoteResponse.isSuccess()) {
            return remoteResponse.getContent();
        }

        System.out.println("[SituationAnalysisAgent] 远程 AI 调用失败: "
                + remoteResponse.getErrorMessage() + "，降级到本地策略分析");

        AnalysisStrategy strategy = AnalysisStrategyFactory.getStrategy(match.getSportType());
        return strategy.analyze(match);
    }

    /**
     * 构建实时态势分析的系统提示词。
     *
     * @param match 比赛实体，用于提取赛事类型、队伍等信息
     * @return 系统提示词字符串
     */
    private String buildSystemPrompt(Match match) {
        return "你是一名专业的体育赛事实时态势分析师。请根据以下比赛数据，生成实时态势分析报告。\n\n"
                + "分析要求：\n"
                + "1. 当前比分和比赛走势\n"
                + "2. 双方队伍的优势与劣势\n"
                + "3. 关键球员的实时表现评价\n"
                + "4. 比赛关键时刻和转折点分析\n"
                + "5. 后续比赛走势预测和战术建议\n\n"
                + "【赛事信息】\n"
                + "赛事类型：" + match.getSportType() + "\n"
                + "比赛状态：" + match.getStatus() + "\n"
                + "甲方：" + (match.getHomeTeam() != null ? match.getHomeTeam().getTeamName() : "未知") + "\n"
                + "乙方：" + (match.getAwayTeam() != null ? match.getAwayTeam().getTeamName() : "未知") + "\n"
                + "当前时间：" + java.time.LocalDateTime.now() + "\n\n"
                + "请用专业但易懂的中文进行分析。";
    }
}
