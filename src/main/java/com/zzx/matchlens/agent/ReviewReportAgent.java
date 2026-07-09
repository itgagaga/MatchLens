package com.zzx.matchlens.agent;

import com.zzx.matchlens.entity.Match;
import org.springframework.stereotype.Component;

/**
 * 赛后复盘报告智能体。
 * <p>
 * 编排数据采集、远程 AI 模型和本地规则三个子 Agent，生成赛后复盘报告。
 * 优先使用远程 AI 模型生成高质量分析；若远程调用失败，
 * 则降级到 {@link LocalRuleAgent} 使用本地规则模板生成报告，
 * 确保任何情况下都能返回可用的复盘内容。
 * </p>
 */
@Component
public class ReviewReportAgent implements AiAgent {

    private final DataCollectAgent dataCollectAgent;
    private final RemoteModelAgent remoteModelAgent;
    private final LocalRuleAgent localRuleAgent;

    /**
     * 构造方法，注入所需的子 Agent 依赖。
     *
     * @param dataCollectAgent 数据采集 Agent，负责收集比赛结构化数据
     * @param remoteModelAgent 远程模型 Agent，负责调用远程 AI 服务
     * @param localRuleAgent   本地规则 Agent，作为降级方案生成模板报告
     */
    public ReviewReportAgent(DataCollectAgent dataCollectAgent,
                             RemoteModelAgent remoteModelAgent,
                             LocalRuleAgent localRuleAgent) {
        this.dataCollectAgent = dataCollectAgent;
        this.remoteModelAgent = remoteModelAgent;
        this.localRuleAgent = localRuleAgent;
    }

    @Override
    public String execute(Match match) {
        return generateReview(match);
    }

    /**
     * 生成赛后复盘报告。
     * <p>
     * 先通过远程 AI 生成报告，若失败则降级到本地规则模板。
     * </p>
     *
     * @param match 待分析的比赛实体
     * @return 复盘报告文本
     */
    public String generateReview(Match match) {
        String systemPrompt = buildSystemPrompt(match);
        String userPrompt = dataCollectAgent.collect(match);

        AiAgentResponse remoteResponse = remoteModelAgent.callWithPrompt(
                match, AiAgentType.REVIEW_REPORT, systemPrompt, userPrompt);

        if (remoteResponse.isSuccess()) {
            return remoteResponse.getContent();
        }

        System.out.println("[ReviewReportAgent] 远程 AI 调用失败: "
                + remoteResponse.getErrorMessage() + "，降级到本地规则模板");
        return localRuleAgent.execute(match);
    }

    /**
     * 构建赛后复盘的系统提示词，包含报告结构和赛事基本信息。
     *
     * @param match 比赛实体，用于提取赛事类型、队伍等信息
     * @return 系统提示词字符串
     */
    private String buildSystemPrompt(Match match) {
        return "你是一名专业的体育赛事分析师。请根据提供的比赛数据，生成一份详细的赛后复盘报告。\n\n"
                + "报告应包含：\n"
                + "1）比赛结果概述\n"
                + "2）关键球员表现分析\n"
                + "3）关键事件回顾\n"
                + "4）胜负原因深度分析\n"
                + "5）针对性改进建议\n\n"
                + "【赛事信息】\n"
                + "赛事类型：" + match.getSportType() + "\n"
                + "比赛状态：" + match.getStatus() + "\n"
                + "甲方：" + (match.getHomeTeam() != null ? match.getHomeTeam().getTeamName() : "未知") + "\n"
                + "乙方：" + (match.getAwayTeam() != null ? match.getAwayTeam().getTeamName() : "未知") + "\n\n"
                + "请使用专业但易懂的中文分析。";
    }
}
