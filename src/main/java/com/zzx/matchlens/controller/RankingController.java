package com.zzx.matchlens.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzx.matchlens.entity.Player;
import com.zzx.matchlens.entity.PlayerStatistics;
import com.zzx.matchlens.mapper.PlayerMapper;
import com.zzx.matchlens.mapper.PlayerStatisticsMapper;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 排行榜接口（支持单场和全局球员排名）
 */
@RestController
@RequestMapping("/api/rankings")
public class RankingController {

    private final PlayerStatisticsMapper playerStatisticsMapper;   // 球员统计 Mapper，提供球员统计表的数据库访问
    private final PlayerMapper playerMapper;                       // 球员 Mapper，用于查询球员名称填充排行榜

    public RankingController(PlayerStatisticsMapper playerStatisticsMapper,
                             PlayerMapper playerMapper) {
        this.playerStatisticsMapper = playerStatisticsMapper;
        this.playerMapper = playerMapper;
    }

    /**
     * 单场比赛球员排行榜
     */
    @GetMapping("/players")
    public List<Map<String, Object>> getPlayerRankings(
            @RequestParam String matchId,
            @RequestParam String statKey,
            @RequestParam(defaultValue = "20") int limit) {

        LambdaQueryWrapper<PlayerStatistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlayerStatistics::getMatchId, matchId)
                .eq(PlayerStatistics::getStatKey, statKey.toUpperCase());
        List<PlayerStatistics> stats = playerStatisticsMapper.selectList(wrapper);

        return buildRanking(stats, limit);
    }

    /**
     * 全局球员排行榜（跨所有比赛汇总）
     */
    @GetMapping("/players/overall")
    public List<Map<String, Object>> getOverallPlayerRankings(
            @RequestParam String statKey,
            @RequestParam(defaultValue = "20") int limit) {

        LambdaQueryWrapper<PlayerStatistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlayerStatistics::getStatKey, statKey.toUpperCase());
        List<PlayerStatistics> stats = playerStatisticsMapper.selectList(wrapper);

        // 按 playerId 汇总
        Map<String, Integer> aggregated = new HashMap<>();
        for (PlayerStatistics s : stats) {
            aggregated.merge(s.getPlayerId(), s.getStatValue(), Integer::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : aggregated.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("playerId", entry.getKey());
            item.put("statValue", entry.getValue());
            result.add(item);
        }
        result.sort((a, b) -> Integer.compare((int) b.get("statValue"), (int) a.get("statValue")));
        if (result.size() > limit) {
            result = result.subList(0, limit);
        }
        fillPlayerNames(result);
        return result;
    }

    /**
     * 构建单场比赛的球员排行榜数据
     */
    private List<Map<String, Object>> buildRanking(List<PlayerStatistics> stats, int limit) {
        // 按 playerId 汇总（同一场比赛同一球员可能有多条同类型统计）
        Map<String, Integer> aggregated = new LinkedHashMap<>();
        for (PlayerStatistics s : stats) {
            aggregated.merge(s.getPlayerId(), s.getStatValue(), Integer::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : aggregated.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("playerId", entry.getKey());
            item.put("statValue", entry.getValue());
            result.add(item);
        }
        result.sort((a, b) -> Integer.compare((int) b.get("statValue"), (int) a.get("statValue")));
        if (result.size() > limit) {
            result = result.subList(0, limit);
        }
        fillPlayerNames(result);
        return result;
    }

    /**
     * 填充排行榜中球员的名称信息
     */
    private void fillPlayerNames(List<Map<String, Object>> result) {
        for (Map<String, Object> item : result) {
            String playerId = (String) item.get("playerId");
            Player player = playerMapper.selectById(playerId);
            if (player != null) {
                item.put("playerName", player.getPlayerName());
            }
        }
    }
}
