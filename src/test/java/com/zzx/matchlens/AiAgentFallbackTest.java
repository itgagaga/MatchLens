package com.zzx.matchlens;

import com.zzx.matchlens.agent.AiAgentType;
import com.zzx.matchlens.agent.RemoteModelAgent;
import com.zzx.matchlens.agent.ReviewReportAgent;
import com.zzx.matchlens.agent.SituationAnalysisAgent;
import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.common.SportType;
import com.zzx.matchlens.dto.*;
import com.zzx.matchlens.entity.AiCallLog;
import com.zzx.matchlens.mapper.AiCallLogMapper;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AiAgentFallbackTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AiCallLogMapper aiCallLogMapper;

    @Autowired
    private RemoteModelAgent remoteModelAgent;

    @Autowired
    private SituationAnalysisAgent situationAnalysisAgent;

    @Autowired
    private ReviewReportAgent reviewReportAgent;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String matchId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        CreateMatchRequest createReq = new CreateMatchRequest();
        createReq.setMatchName("AI测试篮球赛");
        createReq.setSportType(SportType.BASKETBALL);

        MvcResult createResult = mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn();
        matchId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("matchId").asText();

        SetTeamsRequest teamsReq = new SetTeamsRequest();
        teamsReq.setTeamAName("烈焰队");
        teamsReq.setTeamBName("风暴队");
        mockMvc.perform(post("/api/matches/" + matchId + "/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamsReq)))
                .andExpect(status().isOk());

        AddPlayerRequest p1 = new AddPlayerRequest();
        p1.setTeamA(true);
        p1.setPlayerName("张明");
        p1.setNumber(1);
        mockMvc.perform(post("/api/matches/" + matchId + "/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p1)))
                .andExpect(status().isOk());

        AddPlayerRequest p2 = new AddPlayerRequest();
        p2.setTeamA(false);
        p2.setPlayerName("陈杰");
        p2.setNumber(3);
        mockMvc.perform(post("/api/matches/" + matchId + "/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p2)))
                .andExpect(status().isOk());

        MvcResult detail = mockMvc.perform(get("/api/matches/" + matchId))
                .andExpect(status().isOk())
                .andReturn();
        var json = objectMapper.readTree(detail.getResponse().getContentAsString());
        String homeTeamId = json.get("homeTeam").get("teamId").asText();
        String homePlayerId = json.get("homeTeam").get("players").get(0).get("playerId").asText();

        mockMvc.perform(post("/api/matches/" + matchId + "/start"))
                .andExpect(status().isOk());

        for (int i = 0; i < 5; i++) {
            RecordEventRequest eventReq = new RecordEventRequest();
            eventReq.setTeamId(homeTeamId);
            eventReq.setPlayerId(homePlayerId);
            eventReq.setEventType(EventType.SCORE);
            eventReq.setScoreValue(2);
            eventReq.setDescription("得分" + (i + 1));
            mockMvc.perform(post("/api/matches/" + matchId + "/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventReq)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/matches/" + matchId + "/finish"))
                .andExpect(status().isOk());
    }

    /**
     * 测试场景 1：AI 平台正常时，实时态势分析返回远程 AI 内容，并写入成功日志。
     * 如果 DeepSeek API 实际可达，此测试验证远程调用成功路径；
     * 如果 API 不可达，验证降级路径仍然返回结果并写入日志。
     */
    @Test
    @Order(1)
    @DisplayName("实时态势分析：远程 AI 调用或降级，均写入日志")
    void testSituationAnalysis_writesLog() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/matches/" + matchId + "/analysis"))
                .andExpect(status().isOk())
                .andReturn();

        String analysis = result.getResponse().getContentAsString();
        Assertions.assertNotNull(analysis, "态势分析结果不应为 null");
        Assertions.assertFalse(analysis.isEmpty(), "态势分析结果不应为空");
        System.out.println(">>> 态势分析结果:\n" + analysis);

        // 验证日志已写入数据库
        List<AiCallLog> logs = aiCallLogMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiCallLog>()
                        .eq(AiCallLog::getMatchId, matchId)
                        .eq(AiCallLog::getAgentType, AiAgentType.SITUATION_ANALYSIS.name()));
        Assertions.assertFalse(logs.isEmpty(), "应有态势分析的 AI 调用日志");

        AiCallLog log = logs.get(0);
        Assertions.assertNotNull(log.getCallTime(), "日志应记录调用时间");
        Assertions.assertNotNull(log.getResponseTimeMs(), "日志应记录响应耗时");
        System.out.println(">>> AI 调用日志: success=" + log.getSuccess()
                + ", responseTime=" + log.getResponseTimeMs() + "ms"
                + ", error=" + log.getErrorMessage());
    }

    /**
     * 测试场景 2：AI 平台失败或 API Key 缺失时，系统自动降级到本地规则，并写入失败/降级日志。
     * 通过清除 API Key 模拟认证失败，验证降级路径和日志记录。
     */
    @Test
    @Order(2)
    @DisplayName("API Key 缺失时自动降级到本地规则，并写入失败日志")
    void testSituationAnalysis_fallbackToLocal() throws Exception {
        // 清除 API Key 模拟认证失败
        Object originalKey = ReflectionTestUtils.getField(remoteModelAgent, "apiKey");
        ReflectionTestUtils.setField(remoteModelAgent, "apiKey", "");

        try {
            MvcResult result = mockMvc.perform(get("/api/matches/" + matchId + "/analysis"))
                    .andExpect(status().isOk())
                    .andReturn();

            String analysis = result.getResponse().getContentAsString();
            Assertions.assertNotNull(analysis, "降级后态势分析结果不应为 null");
            Assertions.assertFalse(analysis.isEmpty(), "降级后态势分析结果不应为空");
            System.out.println(">>> 降级后态势分析结果:\n" + analysis);

            // 验证写入了失败日志
            List<AiCallLog> logs = aiCallLogMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiCallLog>()
                            .eq(AiCallLog::getMatchId, matchId)
                            .eq(AiCallLog::getAgentType, AiAgentType.SITUATION_ANALYSIS.name())
                            .eq(AiCallLog::getSuccess, 0));
            Assertions.assertFalse(logs.isEmpty(), "应有态势分析的失败降级日志");

            AiCallLog failLog = logs.get(0);
            Assertions.assertEquals(0, failLog.getSuccess(), "日志应标记为失败");
            Assertions.assertNotNull(failLog.getErrorMessage(), "日志应记录失败原因");
            Assertions.assertTrue(failLog.getErrorMessage().contains("API Key"),
                    "失败原因应包含 API Key 相关信息");
            System.out.println(">>> 降级日志: error=" + failLog.getErrorMessage());
        } finally {
            // 恢复原始 API Key
            ReflectionTestUtils.setField(remoteModelAgent, "apiKey", originalKey);
        }
    }
}
