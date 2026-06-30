package com.zzx.matchlens;

import tools.jackson.databind.ObjectMapper;
import com.zzx.matchlens.common.EventType;
import com.zzx.matchlens.common.SportType;
import com.zzx.matchlens.dto.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReportControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String matchId;
    private String homeTeamId;
    private String homePlayerId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 1. 创建比赛
        CreateMatchRequest createReq = new CreateMatchRequest();
        createReq.setMatchName("报告测试篮球赛");
        createReq.setSportType(SportType.BASKETBALL);

        MvcResult createResult = mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn();
        matchId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("matchId").asText();

        // 2. 设置队伍
        SetTeamsRequest teamsReq = new SetTeamsRequest();
        teamsReq.setTeamAName("烈焰队");
        teamsReq.setTeamBName("风暴队");
        mockMvc.perform(post("/api/matches/" + matchId + "/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamsReq)))
                .andExpect(status().isOk());

        // 3. 添加球员
        AddPlayerRequest p1 = new AddPlayerRequest();
        p1.setTeamA(true); p1.setPlayerName("张明"); p1.setNumber(1);
        mockMvc.perform(post("/api/matches/" + matchId + "/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p1)))
                .andExpect(status().isOk());

        AddPlayerRequest p2 = new AddPlayerRequest();
        p2.setTeamA(false); p2.setPlayerName("陈杰"); p2.setNumber(3);
        mockMvc.perform(post("/api/matches/" + matchId + "/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p2)))
                .andExpect(status().isOk());

        // 获取 ID
        MvcResult detail = mockMvc.perform(get("/api/matches/" + matchId))
                .andExpect(status().isOk())
                .andReturn();
        var json = objectMapper.readTree(detail.getResponse().getContentAsString());
        homeTeamId = json.get("homeTeam").get("teamId").asText();
        homePlayerId = json.get("homeTeam").get("players").get(0).get("playerId").asText();

        // 4. 开始比赛
        mockMvc.perform(post("/api/matches/" + matchId + "/start"))
                .andExpect(status().isOk());

        // 5. 录入几条得分事件
        for (int i = 0; i < 3; i++) {
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

        // 6. 结束比赛
        mockMvc.perform(post("/api/matches/" + matchId + "/finish"))
                .andExpect(status().isOk());
    }

    // ==================== 1. 查询统计数据 ====================
    @Test
    @Order(1)
    @DisplayName("GET /api/matches/{id}/statistics — 查询统计数据")
    void testGetStatistics() throws Exception {
        mockMvc.perform(get("/api/matches/" + matchId + "/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.homeScore").value(6))
                .andExpect(jsonPath("$.awayScore").value(0))
                .andExpect(jsonPath("$.eventCount").value(3))
                .andExpect(jsonPath("$.leadingTeam").value("烈焰队"))
                .andExpect(jsonPath("$.scoreDifference").value(6));
    }

    // ==================== 2. 查询态势分析 ====================
    @Test
    @Order(2)
    @DisplayName("GET /api/matches/{id}/analysis — 查询态势分析")
    void testGetAnalysis() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/matches/" + matchId + "/analysis"))
                .andExpect(status().isOk())
                .andReturn();

        String analysis = result.getResponse().getContentAsString();
        System.out.println(">>> 态势分析结果:\n" + analysis);

        // 验证分析结果包含关键信息
        Assertions.assertNotNull(analysis);
        Assertions.assertFalse(analysis.isEmpty());
    }

    // ==================== 3. 生成赛后复盘报告 ====================
    @Test
    @Order(3)
    @DisplayName("GET /api/matches/{id}/report — 生成赛后复盘报告")
    void testGenerateReport() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/matches/" + matchId + "/report"))
                .andExpect(status().isOk())
                .andReturn();

        String report = result.getResponse().getContentAsString();
        System.out.println(">>> 赛后复盘报告:\n" + report);

        // 验证报告包含关键内容
        Assertions.assertNotNull(report);
        Assertions.assertFalse(report.isEmpty());
        Assertions.assertTrue(report.contains("烈焰队"));
        Assertions.assertTrue(report.contains("风暴队"));
    }
}
