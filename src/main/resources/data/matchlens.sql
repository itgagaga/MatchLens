-- ============================================================
-- MatchLens 体育赛事数据智能统计与态势分析系统
-- 数据库初始化脚本
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS matchlens DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE matchlens;

-- ============================================================
-- 1. 建表
-- ============================================================

-- 队伍表
DROP TABLE IF EXISTS t_player_statistics;
DROP TABLE IF EXISTS t_match_event;
DROP TABLE IF EXISTS t_player;
DROP TABLE IF EXISTS t_match;
DROP TABLE IF EXISTS t_team;

CREATE TABLE t_team (
    team_id    VARCHAR(36)  PRIMARY KEY,
    team_name  VARCHAR(100) NOT NULL,
    score      INT          NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 比赛表
CREATE TABLE t_match (
    match_id      VARCHAR(36)  PRIMARY KEY,
    match_name    VARCHAR(100) NOT NULL,
    sport_type    VARCHAR(20)  NOT NULL COMMENT 'BASKETBALL/FOOTBALL/VOLLEYBALL/GENERAL',
    status        VARCHAR(20)  NOT NULL DEFAULT 'NOT_STARTED' COMMENT 'NOT_STARTED/RUNNING/PAUSED/FINISHED',
    home_team_id  VARCHAR(36),
    away_team_id  VARCHAR(36),
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (home_team_id) REFERENCES t_team(team_id),
    FOREIGN KEY (away_team_id) REFERENCES t_team(team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 球员表
CREATE TABLE t_player (
    player_id   VARCHAR(36)  PRIMARY KEY,
    player_name VARCHAR(100) NOT NULL,
    team_id     VARCHAR(36)  NOT NULL,
    number      INT          NOT NULL COMMENT '球衣号码',
    position    VARCHAR(30)  COMMENT '场上位置',
    age         INT          COMMENT '年龄',
    height      INT          COMMENT '身高(cm)',
    weight      INT          COMMENT '体重(kg)',
    FOREIGN KEY (team_id) REFERENCES t_team(team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 比赛事件表
CREATE TABLE t_match_event (
    event_id    VARCHAR(36)  PRIMARY KEY,
    match_id    VARCHAR(36)  NOT NULL,
    team_id     VARCHAR(36)  NOT NULL,
    player_id   VARCHAR(36)  NOT NULL,
    event_type  VARCHAR(20)  NOT NULL COMMENT 'SCORE/FOUL/ASSIST/REBOUND/STEAL/TURNOVER/TIMEOUT等',
    score_value INT          NOT NULL DEFAULT 0,
    event_time  DATETIME     NOT NULL,
    description VARCHAR(500),
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (match_id)  REFERENCES t_match(match_id),
    FOREIGN KEY (team_id)   REFERENCES t_team(team_id),
    FOREIGN KEY (player_id) REFERENCES t_player(player_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 球员统计表 (Key-Value 结构，适配不同赛事统计维度)
CREATE TABLE t_player_statistics (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    player_id  VARCHAR(36)  NOT NULL,
    match_id   VARCHAR(36)  NOT NULL,
    stat_key   VARCHAR(30)  NOT NULL COMMENT '统计维度：SCORE/FOUL/ASSIST/REBOUND/STEAL等',
    stat_value INT          NOT NULL DEFAULT 0,
    FOREIGN KEY (player_id) REFERENCES t_player(player_id),
    FOREIGN KEY (match_id)  REFERENCES t_match(match_id),
    UNIQUE KEY uk_player_match_stat (player_id, match_id, stat_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- AI 调用日志表
DROP TABLE IF EXISTS t_ai_call_log;
CREATE TABLE t_ai_call_log (
    id              VARCHAR(36)  PRIMARY KEY,
    agent_type      VARCHAR(50)  NOT NULL COMMENT 'AI Agent 类型：SITUATION_ANALYSIS/REVIEW_REPORT',
    match_id        VARCHAR(36)  COMMENT '关联比赛 ID',
    prompt          TEXT         COMMENT '发送给 AI 的 prompt',
    response        TEXT         COMMENT 'AI 返回的内容（成功时）',
    success         TINYINT      NOT NULL DEFAULT 0 COMMENT '是否成功：1=成功，0=失败',
    error_message   VARCHAR(500) COMMENT '失败原因（失败时）',
    response_time_ms BIGINT      COMMENT '响应耗时（毫秒）',
    call_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
    INDEX idx_match_id (match_id),
    INDEX idx_agent_type (agent_type),
    INDEX idx_call_time (call_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
-- 2. 插入示例数据 —— 篮球赛（完整演示）
-- ============================================================

-- 队伍
INSERT INTO t_team (team_id, team_name, score) VALUES
('t001', '烈焰队',  92),
('t002', '风暴队',  86);

-- 比赛
INSERT INTO t_match (match_id, match_name, sport_type, status, home_team_id, away_team_id, create_time) VALUES
('m001', '2026赛季篮球联赛第1轮', 'BASKETBALL', 'FINISHED', 't001', 't002', '2026-06-20 19:00:00');

-- 球员 —— 烈焰队（主队）
INSERT INTO t_player (player_id, player_name, team_id, number, position, age, height, weight) VALUES
('p001', '张明',   't001', 1,  '控球后卫', 25, 188, 82),
('p002', '李强',   't001', 7,  '得分后卫', 27, 193, 88),
('p003', '王浩',   't001', 11, '小前锋',   24, 198, 95),
('p004', '赵鹏',   't001', 23, '大前锋',   26, 203, 102),
('p005', '刘洋',   't001', 30, '中锋',     28, 210, 110);

-- 球员 —— 风暴队（客队）
INSERT INTO t_player (player_id, player_name, team_id, number, position, age, height, weight) VALUES
('p006', '陈杰',   't002', 3,  '控球后卫', 23, 185, 78),
('p007', '周涛',   't002', 10, '得分后卫', 26, 190, 85),
('p008', '吴磊',   't002', 15, '小前锋',   25, 196, 92),
('p009', '孙斌',   't002', 21, '大前锋',   27, 201, 100),
('p010', '马飞',   't002', 33, '中锋',     29, 208, 108);

-- 比赛事件 —— 篮球赛完整事件流
INSERT INTO t_match_event (event_id, match_id, team_id, player_id, event_type, score_value, event_time, description) VALUES
-- 第一节
('e001', 'm001', 't001', 'p001', 'SCORE',    2, '2026-06-20 19:02:00', '张明中距离跳投命中'),
('e002', 'm001', 't002', 'p006', 'SCORE',    3, '2026-06-20 19:03:30', '陈杰三分球命中'),
('e003', 'm001', 't001', 'p002', 'ASSIST',   0, '2026-06-20 19:05:00', '李强助攻'),
('e004', 'm001', 't001', 'p001', 'SCORE',    2, '2026-06-20 19:05:10', '张明接助攻上篮得分'),
('e005', 'm001', 't002', 'p007', 'FOUL',     0, '2026-06-20 19:06:00', '周涛防守犯规'),
('e006', 'm001', 't001', 'p003', 'REBOUND',  0, '2026-06-20 19:07:00', '王浩抢下进攻篮板'),
('e007', 'm001', 't001', 'p003', 'SCORE',    2, '2026-06-20 19:07:20', '王浩补篮得分'),
('e008', 'm001', 't002', 'p008', 'SCORE',    2, '2026-06-20 19:09:00', '吴磊突破上篮得分'),
('e009', 'm001', 't002', 'p009', 'FOUL',     0, '2026-06-20 19:10:00', '孙斌进攻犯规'),
('e010', 'm001', 't001', 'p004', 'STEAL',    0, '2026-06-20 19:11:00', '赵鹏抢断成功'),
-- 第二节
('e011', 'm001', 't001', 'p004', 'SCORE',    2, '2026-06-20 19:12:00', '赵鹏快攻扣篮得分'),
('e012', 'm001', 't002', 'p006', 'SCORE',    2, '2026-06-20 19:14:00', '陈杰中投命中'),
('e013', 'm001', 't001', 'p001', 'FOUL',     0, '2026-06-20 19:15:00', '张明防守犯规'),
('e014', 'm001', 't002', 'p010', 'REBOUND',  0, '2026-06-20 19:16:00', '马飞抢下防守篮板'),
('e015', 'm001', 't002', 'p007', 'SCORE',    3, '2026-06-20 19:17:00', '周涛三分球命中'),
('e016', 'm001', 't001', 'p002', 'SCORE',    2, '2026-06-20 19:19:00', '李强突破得分'),
('e017', 'm001', 't001', 'p005', 'TURNOVER', 0, '2026-06-20 19:20:00', '刘洋传球失误'),
('e018', 'm001', 't002', 'p008', 'SCORE',    2, '2026-06-20 19:21:00', '吴磊篮下强打得分'),
('e019', 'm001', 't001', 'p001', 'SCORE',    1, '2026-06-20 19:23:00', '张明罚球命中'),
('e020', 'm001', 't002', 'p009', 'SCORE',    2, '2026-06-20 19:24:00', '孙斌中距离命中'),
-- 第三节
('e021', 'm001', 't001', 'p001', 'SCORE',    2, '2026-06-20 19:32:00', '张明连续得分'),
('e022', 'm001', 't001', 'p001', 'SCORE',    2, '2026-06-20 19:33:30', '张明再中一球'),
('e023', 'm001', 't001', 'p001', 'SCORE',    3, '2026-06-20 19:35:00', '张明三分球命中，连续得分'),
('e024', 'm001', 't002', 'p006', 'FOUL',     0, '2026-06-20 19:36:00', '陈杰犯规'),
('e025', 'm001', 't002', 'p007', 'FOUL',     0, '2026-06-20 19:37:00', '周涛犯规'),
('e026', 'm001', 't001', 'p002', 'SCORE',    2, '2026-06-20 19:38:00', '李强篮下得分'),
('e027', 'm001', 't002', 'p010', 'SCORE',    2, '2026-06-20 19:40:00', '马飞中投命中'),
('e028', 'm001', 't001', 'p003', 'ASSIST',   0, '2026-06-20 19:41:00', '王浩妙传'),
('e029', 'm001', 't001', 'p004', 'SCORE',    2, '2026-06-20 19:41:20', '赵鹏接球得分'),
('e030', 'm001', 't002', 'p008', 'TURNOVER', 0, '2026-06-20 19:43:00', '吴磊运球失误'),
-- 第四节
('e031', 'm001', 't002', 'p006', 'SCORE',    2, '2026-06-20 19:52:00', '陈杰突破得分'),
('e032', 'm001', 't002', 'p007', 'SCORE',    2, '2026-06-20 19:53:30', '周涛中投命中'),
('e033', 'm001', 't002', 'p009', 'SCORE',    3, '2026-06-20 19:55:00', '孙斌三分球命中'),
('e034', 'm001', 't001', 'p005', 'TIMEOUT',  0, '2026-06-20 19:55:30', '烈焰队请求暂停'),
('e035', 'm001', 't001', 'p001', 'SCORE',    2, '2026-06-20 19:58:00', '张明关键中投命中'),
('e036', 'm001', 't001', 'p002', 'REBOUND',  0, '2026-06-20 19:59:00', '李强关键防守篮板'),
('e037', 'm001', 't002', 'p008', 'FOUL',     0, '2026-06-20 20:00:00', '吴磊犯规'),
('e038', 'm001', 't001', 'p004', 'SCORE',    1, '2026-06-20 20:01:00', '赵鹏罚球命中'),
('e039', 'm001', 't002', 'p010', 'SCORE',    2, '2026-06-20 20:03:00', '马飞篮下得分'),
('e040', 'm001', 't001', 'p001', 'SCORE',    2, '2026-06-20 20:05:00', '张明锁定胜局');

-- 球员统计 —— 烈焰队
INSERT INTO t_player_statistics (player_id, match_id, stat_key, stat_value) VALUES
-- 张明 #1: 28分, 1篮板, 1犯规
('p001', 'm001', 'SCORE',    28),
('p001', 'm001', 'REBOUND',   1),
('p001', 'm001', 'FOUL',      1),
-- 李强 #7: 6分, 1助攻, 2篮板
('p002', 'm001', 'SCORE',     6),
('p002', 'm001', 'ASSIST',    1),
('p002', 'm001', 'REBOUND',   2),
-- 王浩 #11: 2分, 2助攻, 1篮板
('p003', 'm001', 'SCORE',     2),
('p003', 'm001', 'ASSIST',    2),
('p003', 'm001', 'REBOUND',   1),
-- 赵鹏 #23: 7分, 1抢断
('p004', 'm001', 'SCORE',     7),
('p004', 'm001', 'STEAL',     1),
-- 刘洋 #30: 0分, 1失误, 1暂停
('p005', 'm001', 'SCORE',     0),
('p005', 'm001', 'TURNOVER',  1),
('p005', 'm001', 'TIMEOUT',   1);

-- 球员统计 —— 风暴队
INSERT INTO t_player_statistics (player_id, match_id, stat_key, stat_value) VALUES
-- 陈杰 #3: 7分, 1犯规
('p006', 'm001', 'SCORE',     7),
('p006', 'm001', 'FOUL',      1),
-- 周涛 #10: 8分, 2犯规
('p007', 'm001', 'SCORE',     8),
('p007', 'm001', 'FOUL',      2),
-- 吴磊 #15: 4分, 1失误, 1犯规
('p008', 'm001', 'SCORE',     4),
('p008', 'm001', 'TURNOVER',  1),
('p008', 'm001', 'FOUL',      1),
-- 孙斌 #21: 5分, 1犯规
('p009', 'm001', 'SCORE',     5),
('p009', 'm001', 'FOUL',      1),
-- 马飞 #33: 4分, 1篮板
('p010', 'm001', 'SCORE',     4),
('p010', 'm001', 'REBOUND',   1);


-- ============================================================
-- 3. 插入示例数据 —— 足球赛（轻量适配）
-- ============================================================

-- 队伍
INSERT INTO t_team (team_id, team_name, score) VALUES
('t003', '青龙队', 2),
('t004', '白虎队', 1);

-- 比赛
INSERT INTO t_match (match_id, match_name, sport_type, status, home_team_id, away_team_id, create_time) VALUES
('m002', '2026赛季足球联赛第1轮', 'FOOTBALL', 'FINISHED', 't003', 't004', '2026-06-21 15:00:00');

-- 球员 —— 青龙队
INSERT INTO t_player (player_id, player_name, team_id, number, position, age, height, weight) VALUES
('p011', '林峰',   't003', 1,  '门将',   28, 190, 85),
('p012', '黄磊',   't003', 5,  '后卫',   26, 183, 78),
('p013', '杨帆',   't003', 9,  '前锋',   24, 180, 75),
('p014', '徐亮',   't003', 10, '中场',   25, 178, 72),
('p015', '何伟',   't003', 7,  '中场',   27, 182, 76);

-- 球员 —— 白虎队
INSERT INTO t_player (player_id, player_name, team_id, number, position, age, height, weight) VALUES
('p016', '郑凯',   't004', 1,  '门将',   29, 192, 88),
('p017', '罗杰',   't004', 4,  '后卫',   25, 185, 80),
('p018', '唐浩',   't004', 8,  '中场',   26, 179, 74),
('p019', '韩超',   't004', 11, '前锋',   23, 181, 77),
('p020', '冯鑫',   't004', 14, '后卫',   27, 184, 79);

-- 比赛事件 —— 足球赛
INSERT INTO t_match_event (event_id, match_id, team_id, player_id, event_type, score_value, event_time, description) VALUES
('e041', 'm002', 't003', 'p013', 'SCORE',       1, '2026-06-21 15:23:00', '杨帆头球破门'),
('e042', 'm002', 't004', 'p018', 'FOUL',        0, '2026-06-21 15:30:00', '唐浩中场犯规'),
('e043', 'm002', 't004', 'p019', 'YELLOW_CARD', 0, '2026-06-21 15:35:00', '韩超恶意铲球被黄牌警告'),
('e044', 'm002', 't003', 'p014', 'SCORE',       1, '2026-06-21 15:58:00', '徐亮远射破门'),
('e045', 'm002', 't004', 'p017', 'FOUL',        0, '2026-06-21 16:10:00', '罗杰犯规'),
('e046', 'm002', 't004', 'p017', 'RED_CARD',    0, '2026-06-21 16:12:00', '罗杰累计两张黄牌被红牌罚下'),
('e047', 'm002', 't003', 'p015', 'SUBSTITUTION',0, '2026-06-21 16:30:00', '何伟被换下'),
('e048', 'm002', 't004', 'p019', 'SCORE',       1, '2026-06-21 16:45:00', '韩超任意球扳回一球'),
('e049', 'm002', 't003', 'p012', 'FOUL',        0, '2026-06-21 16:50:00', '黄磊防守犯规'),
('e050', 'm002', 't004', 'p018', 'YELLOW_CARD', 0, '2026-06-21 16:55:00', '唐浩抗议判罚被黄牌警告');

-- 球员统计 —— 青龙队
INSERT INTO t_player_statistics (player_id, match_id, stat_key, stat_value) VALUES
('p013', 'm002', 'SCORE',       1),
('p014', 'm002', 'SCORE',       1),
('p012', 'm002', 'FOUL',        1);

-- 球员统计 —— 白虎队
INSERT INTO t_player_statistics (player_id, match_id, stat_key, stat_value) VALUES
('p019', 'm002', 'SCORE',       1),
('p018', 'm002', 'FOUL',        1),
('p018', 'm002', 'YELLOW_CARD', 1),
('p017', 'm002', 'RED_CARD',    1),
('p019', 'm002', 'YELLOW_CARD', 1);


-- ============================================================
-- 4. 插入示例数据 —— 排球赛（轻量适配）
-- ============================================================

-- 队伍
INSERT INTO t_team (team_id, team_name, score) VALUES
('t005', '闪电队', 3),
('t006', '雷霆队', 1);

-- 比赛
INSERT INTO t_match (match_id, match_name, sport_type, status, home_team_id, away_team_id, create_time) VALUES
('m003', '2026赛季排球联赛第1轮', 'VOLLEYBALL', 'FINISHED', 't005', 't006', '2026-06-22 18:00:00');

-- 球员 —— 闪电队
INSERT INTO t_player (player_id, player_name, team_id, number, position, age, height, weight) VALUES
('p021', '田宇',   't005', 1,  '主攻手', 24, 195, 88),
('p022', '贺磊',   't005', 5,  '副攻手', 26, 198, 92),
('p023', '邓超',   't005', 8,  '接应',   25, 196, 90),
('p024', '彭涛',   't005', 12, '二传手', 27, 190, 82);

-- 球员 —— 雷霆队
INSERT INTO t_player (player_id, player_name, team_id, number, position, age, height, weight) VALUES
('p025', '谢斌',   't006', 2,  '主攻手', 23, 193, 85),
('p026', '苏杰',   't006', 6,  '副攻手', 25, 197, 91),
('p027', '魏亮',   't006', 9,  '接应',   24, 194, 87),
('p028', '秦浩',   't006', 14, '二传手', 26, 189, 80);

-- 比赛事件 —— 排球赛
INSERT INTO t_match_event (event_id, match_id, team_id, player_id, event_type, score_value, event_time, description) VALUES
('e051', 'm003', 't005', 'p021', 'SERVE_ACE', 1, '2026-06-22 18:05:00', '田宇发球直接得分'),
('e052', 'm003', 't005', 'p022', 'BLOCK',     1, '2026-06-22 18:08:00', '贺磊拦网得分'),
('e053', 'm003', 't006', 'p025', 'SCORE',     1, '2026-06-22 18:10:00', '谢斌扣球得分'),
('e054', 'm003', 't006', 'p026', 'ERROR',     0, '2026-06-22 18:12:00', '苏杰发球失误'),
('e055', 'm003', 't005', 'p023', 'SERVE_ACE', 1, '2026-06-22 18:15:00', '邓超发球得分'),
('e056', 'm003', 't005', 'p024', 'BLOCK',     1, '2026-06-22 18:18:00', '彭涛拦网得分'),
('e057', 'm003', 't006', 'p027', 'SCORE',     1, '2026-06-22 18:20:00', '魏亮扣球得分'),
('e058', 'm003', 't006', 'p028', 'ERROR',     0, '2026-06-22 18:22:00', '秦浩扣球出界'),
('e059', 'm003', 't005', 'p021', 'SCORE',     1, '2026-06-22 18:25:00', '田宇扣球得分'),
('e060', 'm003', 't005', 'p022', 'SERVE_ACE', 1, '2026-06-22 18:28:00', '贺磊发球直接得分'),
('e061', 'm003', 't006', 'p026', 'BLOCK',     1, '2026-06-22 18:30:00', '苏杰拦网得分'),
('e062', 'm003', 't006', 'p025', 'ERROR',     0, '2026-06-22 18:32:00', '谢斌发球下网'),
('e063', 'm003', 't005', 'p023', 'SCORE',     1, '2026-06-22 18:35:00', '邓超关键扣球得分'),
('e064', 'm003', 't006', 'p027', 'ERROR',     0, '2026-06-22 18:38:00', '魏亮触网犯规'),
('e065', 'm003', 't005', 'p024', 'SCORE',     1, '2026-06-22 18:40:00', '彭涛扣球锁定胜局');

-- 球员统计 —— 闪电队
INSERT INTO t_player_statistics (player_id, match_id, stat_key, stat_value) VALUES
('p021', 'm003', 'SCORE',     2),
('p021', 'm003', 'SERVE_ACE', 1),
('p022', 'm003', 'SCORE',     0),
('p022', 'm003', 'BLOCK',     1),
('p022', 'm003', 'SERVE_ACE', 1),
('p023', 'm003', 'SCORE',     2),
('p023', 'm003', 'SERVE_ACE', 1),
('p024', 'm003', 'SCORE',     1),
('p024', 'm003', 'BLOCK',     1);

-- 球员统计 —— 雷霆队
INSERT INTO t_player_statistics (player_id, match_id, stat_key, stat_value) VALUES
('p025', 'm003', 'SCORE',     1),
('p025', 'm003', 'ERROR',     1),
('p026', 'm003', 'BLOCK',     1),
('p026', 'm003', 'ERROR',     1),
('p027', 'm003', 'SCORE',     1),
('p027', 'm003', 'ERROR',     1),
('p028', 'm003', 'ERROR',     1);
