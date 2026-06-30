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
class EventControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String matchId;
    private String homeTeamId;
    private String awayTeamId;
    private String homePlayerId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 1. 创建比赛
        CreateMatchRequest createReq = new CreateMatchRequest();
        createReq.setMatchName("事件测试篮球赛");
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

        // 获取队伍 ID
        MvcResult matchResult = mockMvc.perform(get("/api/matches/" + matchId))
                .andExpect(status().isOk())
                .andReturn();
        var matchJson = objectMapper.readTree(matchResult.getResponse().getContentAsString());
        homeTeamId = matchJson.get("homeTeam").get("teamId").asText();
        awayTeamId = matchJson.get("awayTeam").get("teamId").asText();

        // 3. 添加球员
        AddPlayerRequest playerReq1 = new AddPlayerRequest();
        playerReq1.setTeamA(true);
        playerReq1.setPlayerName("张明");
        playerReq1.setNumber(1);
        mockMvc.perform(post("/api/matches/" + matchId + "/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(playerReq1)))
                .andExpect(status().isOk());

        AddPlayerRequest playerReq2 = new AddPlayerRequest();
        playerReq2.setTeamA(false);
        playerReq2.setPlayerName("陈杰");
        playerReq2.setNumber(3);
        mockMvc.perform(post("/api/matches/" + matchId + "/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(playerReq2)))
                .andExpect(status().isOk());

        // 获取甲方球员 ID
        MvcResult detailResult = mockMvc.perform(get("/api/matches/" + matchId))
                .andExpect(status().isOk())
                .andReturn();
        var detailJson = objectMapper.readTree(detailResult.getResponse().getContentAsString());
        homePlayerId = detailJson.get("homeTeam").get("players").get(0).get("playerId").asText();

        // 4. 开始比赛
        mockMvc.perform(post("/api/matches/" + matchId + "/start"))
                .andExpect(status().isOk());
    }

    // ==================== 录入得分事件 ====================
    @Test
    @Order(1)
    @DisplayName("POST /api/matches/{id}/events — 录入得分事件")
    void testRecordScoreEvent() throws Exception {
        RecordEventRequest request = new RecordEventRequest();
        request.setTeamId(homeTeamId);
        request.setPlayerId(homePlayerId);
        request.setEventType(EventType.SCORE);
        request.setScoreValue(2);
        request.setDescription("中距离跳投命中");

        mockMvc.perform(post("/api/matches/" + matchId + "/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("事件录入成功"));
    }

    // ==================== 录入犯规事件 ====================
    @Test
    @Order(2)
    @DisplayName("POST /api/matches/{id}/events — 录入犯规事件")
    void testRecordFoulEvent() throws Exception {
        RecordEventRequest request = new RecordEventRequest();
        request.setTeamId(homeTeamId);
        request.setPlayerId(homePlayerId);
        request.setEventType(EventType.FOUL);
        request.setScoreValue(0);
        request.setDescription("防守犯规");

        mockMvc.perform(post("/api/matches/" + matchId + "/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== 录入三分球 ====================
    @Test
    @Order(3)
    @DisplayName("POST /api/matches/{id}/events — 录入三分球事件")
    void testRecordThreePointEvent() throws Exception {
        RecordEventRequest request = new RecordEventRequest();
        request.setTeamId(homeTeamId);
        request.setPlayerId(homePlayerId);
        request.setEventType(EventType.SCORE);
        request.setScoreValue(3);
        request.setDescription("三分球命中");

        mockMvc.perform(post("/api/matches/" + matchId + "/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== 校验：非法得分值（篮球只能1/2/3） ====================
    @Test
    @Order(4)
    @DisplayName("POST /api/matches/{id}/events — 校验非法得分值")
    void testInvalidScoreValue() throws Exception {
        RecordEventRequest request = new RecordEventRequest();
        request.setTeamId(homeTeamId);
        request.setPlayerId(homePlayerId);
        request.setEventType(EventType.SCORE);
        request.setScoreValue(5); // 篮球不允许5分
        request.setDescription("非法得分");

        mockMvc.perform(post("/api/matches/" + matchId + "/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== 校验：未开始比赛不能录入事件 ====================
    @Test
    @Order(5)
    @DisplayName("校验：未开始比赛不能录入事件")
    void testRecordEventOnNotStartedMatch() throws Exception {
        // 创建一个新比赛（未开始状态）
        CreateMatchRequest createReq = new CreateMatchRequest();
        createReq.setMatchName("未开始比赛");
        createReq.setSportType(SportType.BASKETBALL);

        MvcResult createResult = mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn();
        String newMatchId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("matchId").asText();

        RecordEventRequest request = new RecordEventRequest();
        request.setTeamId("any-team");
        request.setPlayerId("any-player");
        request.setEventType(EventType.SCORE);
        request.setScoreValue(2);

        mockMvc.perform(post("/api/matches/" + newMatchId + "/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== 录入事件后验证比分更新 ====================
    @Test
    @Order(6)
    @DisplayName("录入得分事件后比分自动更新")
    void testScoreUpdateAfterEvent() throws Exception {
        // 录入一个 2 分事件
        RecordEventRequest request = new RecordEventRequest();
        request.setTeamId(homeTeamId);
        request.setPlayerId(homePlayerId);
        request.setEventType(EventType.SCORE);
        request.setScoreValue(2);
        request.setDescription("上篮得分");

        mockMvc.perform(post("/api/matches/" + matchId + "/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 查询比赛详情，验证比分
        mockMvc.perform(get("/api/matches/" + matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.homeTeam.score").value(2));
    }
}
