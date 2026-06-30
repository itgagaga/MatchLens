package com.zzx.matchlens;

import tools.jackson.databind.ObjectMapper;
import com.zzx.matchlens.common.SportType;
import com.zzx.matchlens.dto.AddPlayerRequest;
import com.zzx.matchlens.dto.CreateMatchRequest;
import com.zzx.matchlens.dto.SetTeamsRequest;
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
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MatchControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // 跨方法共享 matchId
    private static String matchId;

    // ==================== 1. 创建比赛 ====================
    @Test
    @Order(1)
    @DisplayName("POST /api/matches — 创建比赛")
    void testCreateMatch() throws Exception {
        CreateMatchRequest request = new CreateMatchRequest();
        request.setMatchName("测试篮球赛");
        request.setSportType(SportType.BASKETBALL);

        MvcResult result = mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId").exists())
                .andExpect(jsonPath("$.matchName").value("测试篮球赛"))
                .andExpect(jsonPath("$.sportType").value("BASKETBALL"))
                .andExpect(jsonPath("$.status").value("NOT_STARTED"))
                .andReturn();

        // 提取 matchId 供后续测试使用
        String json = result.getResponse().getContentAsString();
        matchId = objectMapper.readTree(json).get("matchId").asText();
        System.out.println(">>> 创建比赛成功, matchId = " + matchId);
    }

    // ==================== 2. 查询所有比赛 ====================
    @Test
    @Order(2)
    @DisplayName("GET /api/matches — 查询所有比赛")
    void testGetAllMatches() throws Exception {
        // 先创建一条数据
        createTestMatch();

        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    // ==================== 3. 查询单场比赛 ====================
    @Test
    @Order(3)
    @DisplayName("GET /api/matches/{id} — 查询单场比赛")
    void testGetMatch() throws Exception {
        String id = createTestMatch();

        mockMvc.perform(get("/api/matches/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId").value(id))
                .andExpect(jsonPath("$.matchName").value("查询测试赛"));
    }

    // ==================== 4. 设置队伍 ====================
    @Test
    @Order(4)
    @DisplayName("POST /api/matches/{id}/teams — 设置甲方和乙方")
    void testSetTeams() throws Exception {
        String id = createTestMatch();

        SetTeamsRequest request = new SetTeamsRequest();
        request.setTeamAName("测试甲方");
        request.setTeamBName("测试乙方");

        mockMvc.perform(post("/api/matches/" + id + "/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("队伍设置成功"));

        // 验证队伍已设置
        mockMvc.perform(get("/api/matches/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamA.teamName").value("测试甲方"))
                .andExpect(jsonPath("$.teamB.teamName").value("测试乙方"));
    }

    // ==================== 5. 添加球员 ====================
    @Test
    @Order(5)
    @DisplayName("POST /api/matches/{id}/players — 添加球员")
    void testAddPlayer() throws Exception {
        String id = createMatchWithTeams();

        AddPlayerRequest request = new AddPlayerRequest();
        request.setTeamA(true);
        request.setPlayerName("测试球员");
        request.setNumber(23);

        mockMvc.perform(post("/api/matches/" + id + "/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== 6. 开始比赛 ====================
    @Test
    @Order(6)
    @DisplayName("POST /api/matches/{id}/start — 开始比赛")
    void testStartMatch() throws Exception {
        String id = createMatchWithTeamsAndPlayers();

        mockMvc.perform(post("/api/matches/" + id + "/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("比赛已开始"));

        // 验证状态变为 RUNNING
        mockMvc.perform(get("/api/matches/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"));
    }

    // ==================== 7. 暂停比赛 ====================
    @Test
    @Order(7)
    @DisplayName("POST /api/matches/{id}/pause — 暂停比赛")
    void testPauseMatch() throws Exception {
        String id = createRunningMatch();

        mockMvc.perform(post("/api/matches/" + id + "/pause"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("比赛已暂停"));

        // 验证状态变为 PAUSED
        mockMvc.perform(get("/api/matches/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAUSED"));
    }

    // ==================== 8. 恢复比赛 ====================
    @Test
    @Order(8)
    @DisplayName("POST /api/matches/{id}/resume — 恢复比赛")
    void testResumeMatch() throws Exception {
        String id = createRunningMatch();

        // 先暂停
        mockMvc.perform(post("/api/matches/" + id + "/pause"))
                .andExpect(status().isOk());

        // 再恢复
        mockMvc.perform(post("/api/matches/" + id + "/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("比赛已恢复"));

        // 验证状态变回 RUNNING
        mockMvc.perform(get("/api/matches/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"));
    }

    // ==================== 9. 结束比赛 ====================
    @Test
    @Order(9)
    @DisplayName("POST /api/matches/{id}/finish — 结束比赛")
    void testFinishMatch() throws Exception {
        String id = createRunningMatch();

        mockMvc.perform(post("/api/matches/" + id + "/finish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("比赛已结束"));

        // 验证状态变为 FINISHED
        mockMvc.perform(get("/api/matches/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"));
    }

    // ==================== 辅助方法 ====================

    private String createTestMatch() throws Exception {
        CreateMatchRequest request = new CreateMatchRequest();
        request.setMatchName("查询测试赛");
        request.setSportType(SportType.BASKETBALL);

        MvcResult result = mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("matchId").asText();
    }

    private String createMatchWithTeams() throws Exception {
        String id = createTestMatch();

        SetTeamsRequest teamsReq = new SetTeamsRequest();
        teamsReq.setTeamAName("甲方A");
        teamsReq.setTeamBName("乙方B");

        mockMvc.perform(post("/api/matches/" + id + "/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamsReq)))
                .andExpect(status().isOk());

        return id;
    }

    private String createMatchWithTeamsAndPlayers() throws Exception {
        String id = createMatchWithTeams();

        AddPlayerRequest playerReq = new AddPlayerRequest();
        playerReq.setTeamA(true);
        playerReq.setPlayerName("球员1");
        playerReq.setNumber(1);

        mockMvc.perform(post("/api/matches/" + id + "/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(playerReq)))
                .andExpect(status().isOk());

        return id;
    }

    private String createRunningMatch() throws Exception {
        String id = createMatchWithTeamsAndPlayers();

        mockMvc.perform(post("/api/matches/" + id + "/start"))
                .andExpect(status().isOk());

        return id;
    }
}
