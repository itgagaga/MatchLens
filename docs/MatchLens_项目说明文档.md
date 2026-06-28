# MatchLens 体育赛事数据智能统计与态势分析系统项目说明文档

必须体现 AI Agent 分析与复盘能力；不要求你自研 AI，也不要求复杂训练模型。

## 一、项目基本信息

**选题名称**：AI Agent 体育赛事数据智能统计与态势分析系统  
**主要开发语言**：Java  
**主要实现方向**：通用赛事模型 + 多赛事策略适配 + AI Agent 业务封装 + 设计模式约束架构  

本项目面向体育赛事数据统计与态势分析场景，设计并实现一个支持多类体育赛事的数据统计、态势分析与赛后复盘系统。系统不以某一种赛事作为唯一模型，而是抽象出通用赛事对象，例如 `Match`、`Team`、`Player`、`MatchEvent`、`MatchStatistics` 等，再通过策略模式适配篮球、足球、排球等不同赛事的统计与分析规则。

本项目以篮球赛事作为完整演示对象，完整实现比赛创建、事件录入、数据统计、实时态势分析和赛后复盘流程；同时以足球、排球作为轻量适配赛事，通过不同分析策略体现系统的多赛事扩展能力。

---

## 二、文档要求理解

核心要求可以概括为以下几点：

### 2.1 AI Agent 模块要求

本项目需要封装赛事数据采集、态势分析、数据复盘等 AI Agent 模块。这里的 AI Agent 不要求自研大模型或底层智能体算法，而是将已有 AI 能力或规则分析能力封装为系统中的独立业务模块。

AI Agent 模块的重点是：

1. 与业务代码解耦；
2. 有统一接口；
3. 可替换、可扩展；
4. 能根据赛事数据生成分析结果；
5. 体现 AI 能力调用与业务封装，而不是简单把 API 调用写死在业务方法中。

### 2.2 设计模式要求

课程设计要求使用不少于 3 种设计模式，并通过设计模式约束 Vibe Coding 增量开发过程，避免代码堆砌、架构混乱、状态错乱和统计规则混乱。

明确提到适合使用以下设计模式：

1. 责任链模式；
2. 策略模式；
3. 状态模式；
4. 观察者模式。

本项目将完整采用以上 4 种设计模式或更多。

### 2.3 多赛事适配要求

指导书要求系统“适配多类体育赛事”，但没有规定必须完整实现多少种体育赛事。因此，本项目采用较稳妥、工作量可控的实现方式：

1. 篮球：完整实现；
2. 足球：轻量适配；
3. 排球：轻量适配；
4. 通用赛事：兜底策略。

这样既能体现多赛事适配能力，又不会因为完整实现过多赛事导致项目复杂度过高。

---

## 三、系统总体定位

### 3.1 系统要解决的问题

体育赛事数据统计与态势分析系统需要处理多个问题：

1. 不同赛事的统计规则不同；
2. 比赛状态容易混乱，例如比赛结束后仍然录入事件；
3. 比赛事件数据需要校验，例如得分不能为负数；
4. 比赛事件发生后，比分、统计、预警、复盘等模块都需要同步更新；
5. AI 分析模块不能与业务逻辑强耦合；
6. 后续新增赛事类型时，不能大面积修改核心代码。

因此，本项目通过通用模型和设计模式构建一个可扩展的赛事分析系统。

### 3.2 系统核心目标

系统需要实现以下目标：

1. 支持创建和管理体育比赛；
2. 支持录入不同赛事的比赛事件；
3. 自动校验比赛事件是否合法；
4. 自动更新比分和统计数据；
5. 根据赛事类型选择不同分析策略；
6. 生成实时态势分析；
7. 比赛结束后生成赛后复盘报告；
8. 通过 AI Agent 模块封装分析和复盘能力；
9. 通过设计模式保证系统低耦合、高内聚、可扩展。

---

## 四、推荐适配赛事

### 4.1 推荐赛事组合

本项目推荐适配以下 3 类赛事：

| 赛事 | 实现程度 | 说明 |
|---|---|---|
| 篮球 Basketball | 完整实现 | 作为主要演示赛事，完整实现数据录入、统计、分析、复盘 |
| 足球 Football | 轻量适配 | 通过策略类体现不同事件和分析规则 |
| 排球 Volleyball | 轻量适配 | 通过策略类体现不同事件和分析规则 |

### 4.2 为什么选择这三类赛事

选择篮球、足球、排球的原因是：

1. 都属于团队对抗类赛事；
2. 都可以抽象为两支队伍之间的比赛；
3. 都存在比分、事件、球员、队伍等通用概念；
4. 可以共用 `Match`、`Team`、`Player`、`MatchEvent` 等核心实体；
5. 赛事差异主要体现在事件类型和分析策略上，适合用策略模式解决。

不推荐优先选择网球、田径、游泳等赛事，因为它们的数据结构和计分规则差异较大，容易让项目模型复杂化。

---

## 五、系统功能需求

### 5.1 比赛管理模块

比赛管理模块负责创建和维护比赛基本信息。

主要功能包括：

1. 创建比赛；
2. 设置比赛类型；
3. 添加参赛队伍；
4. 添加参赛球员；
5. 开始比赛；
6. 暂停比赛；
7. 恢复比赛；
8. 结束比赛；
9. 查看比赛基本信息。

比赛状态包括：

1. 未开始；
2. 进行中；
3. 暂停中；
4. 已结束。

### 5.2 赛事数据采集模块

赛事数据采集模块负责接收比赛过程中的事件数据。

本项目不强制接入真实赛事接口，采用手动录入和模拟数据导入即可。用户可以录入比赛事件，例如：

篮球事件：

1. 得分；
2. 犯规；
3. 助攻；
4. 篮板；
5. 抢断；
6. 失误；
7. 暂停。

足球事件：

1. 进球；
2. 犯规；
3. 黄牌；
4. 红牌；
5. 换人。

排球事件：

1. 得分；
2. 拦网；
3. 发球得分；
4. 失误；
5. 暂停。

这些事件进入系统后，需要经过责任链校验，再进入统计和分析流程。

### 5.3 数据统计模块

数据统计模块负责根据比赛事件自动更新统计数据。

通用统计内容包括：

1. 当前比分；
2. 队伍得分；
3. 球员得分；
4. 犯规次数；
5. 关键事件数量；
6. 当前领先方；
7. 当前分差；
8. 比赛事件列表。

篮球扩展统计可以包括：

1. 篮板数；
2. 助攻数；
3. 抢断数；
4. 失误数；
5. 球员得分榜。

足球扩展统计可以包括：

1. 进球数；
2. 红黄牌数量；
3. 换人次数；
4. 犯规次数。

排球扩展统计可以包括：

1. 发球得分；
2. 拦网得分；
3. 失误次数；
4. 局分变化。

### 5.4 态势分析模块

态势分析模块根据当前比赛数据判断比赛走势。

例如篮球比赛中可以分析：

1. 哪支队伍领先；
2. 当前分差是否较大；
3. 是否进入焦灼阶段；
4. 某队是否连续得分；
5. 某队犯规是否过多；
6. 某球员是否表现突出。

示例输出：

> 当前 A 队领先 8 分，并且最近连续得分，比赛主动权偏向 A 队。B 队犯规次数较多，防守压力增大，如果不能减少失误，后续存在被进一步拉开比分的风险。

### 5.5 赛后复盘模块

比赛结束后，系统调用复盘 Agent 生成赛后复盘报告。

复盘报告包括：

1. 比赛基本信息；
2. 最终比分；
3. 胜负结果；
4. 关键球员表现；
5. 关键事件回顾；
6. 胜负原因分析；
7. 后续改进建议。

示例输出：

> 本场比赛 A 队以 92:86 战胜 B 队。A 队在第三节通过连续快攻建立领先优势，核心球员 1 号贡献 28 分，是获胜关键。B 队虽然末节缩小分差，但由于失误偏多，最终未能完成反超。建议 B 队后续加强控球稳定性和防守轮转。

### 5.6 报告输出模块

报告输出模块负责整合比赛数据、统计结果、态势分析和赛后复盘，生成完整比赛报告。

报告内容包括：

1. 比赛名称；
2. 赛事类型；
3. 比赛状态；
4. 参赛队伍；
5. 最终比分；
6. 关键统计数据；
7. 实时态势分析；
8. 赛后复盘内容。

---

## 六、系统非功能需求

系统应满足以下非功能性需求：

1. 架构清晰，分层明确；
2. 核心实体通用，不写死某一种赛事；
3. 模块之间低耦合；
4. 代码命名规范，注释清楚；
5. 新增赛事时尽量不修改核心代码；
6. AI Agent 模块可替换；
7. 数据校验流程可扩展；
8. 比赛状态流转可靠；
9. 系统运行稳定；
10. 便于后续扩展真实赛事 API。

---

## 七、系统总体架构设计

### 7.1 分层架构

系统采用五层结构：

```text
表现层
  ↓
业务服务层
  ↓
设计模式核心层
  ↓
AI Agent 封装层
  ↓
数据层
```

### 7.2 各层职责

#### 7.2.1 表现层

负责用户交互和结果展示，可以是控制台、简单 Web 页面或接口测试页面。

主要职责：

1. 输入比赛信息；
2. 输入比赛事件；
3. 查看统计结果；
4. 查看态势分析；
5. 查看赛后复盘。

#### 7.2.2 业务服务层

负责核心业务流程编排。

主要类包括：

1. `MatchService`：比赛管理；
2. `EventService`：事件录入；
3. `StatisticsService`：数据统计；
4. `ReportService`：报告生成。

#### 7.2.3 设计模式核心层

负责落地状态、责任链、策略、观察者模式。

主要包包括：

1. `state`；
2. `chain`；
3. `strategy`；
4. `observer`。

#### 7.2.4 AI Agent 封装层

负责赛事态势分析和赛后复盘。

主要类包括：

1. `AiAgent`；
2. `DataCollectAgent`；
3. `SituationAnalysisAgent`；
4. `ReviewReportAgent`；
5. `LocalRuleAgent`；
6. `RemoteModelAgent`。

#### 7.2.5 数据层

负责存储比赛、队伍、球员、事件和统计结果。

本项目采用 **MySQL 关系型数据库** 作为持久化存储，通过 Spring Data JPA 进行 ORM 映射。数据库共包含 5 张核心表：

| 表名 | 说明 | 关键字段 |
|---|---|---|
| `t_match` | 比赛表 | match_id, match_name, sport_type, status, home_team_id, away_team_id |
| `t_team` | 队伍表 | team_id, team_name, score |
| `t_player` | 球员表 | player_id, player_name, team_id, number |
| `t_match_event` | 比赛事件表 | event_id, match_id, team_id, player_id, event_type, score_value, event_time |
| `t_player_statistics` | 球员统计表 | id, player_id, match_id, stat_key, stat_value |

其中 `MatchStatistics` 为运行时计算对象，不单独建表，通过聚合 `t_team`、`t_player_statistics`、`t_match_event` 的数据实时生成。

---

## 八、Java 包结构设计

```text
matchlens
├── common
│   ├── SportType.java
│   ├── EventType.java
│   ├── MatchStatus.java
│   └── Result.java
│
├── entity
│   ├── Match.java
│   ├── Team.java
│   ├── Player.java
│   ├── MatchEvent.java
│   └── MatchStatistics.java
│
├── service
│   ├── MatchService.java
│   ├── EventService.java
│   ├── StatisticsService.java
│   └── ReportService.java
│
├── state
│   ├── MatchState.java
│   ├── NotStartedState.java
│   ├── RunningState.java
│   ├── PausedState.java
│   └── FinishedState.java
│
├── chain
│   ├── EventCheckHandler.java
│   ├── BasicEventCheckHandler.java
│   ├── StateCheckHandler.java
│   ├── PlayerCheckHandler.java
│   ├── ScoreCheckHandler.java
│   └── EventTypeCheckHandler.java
│
├── strategy
│   ├── AnalysisStrategy.java
│   ├── BasketballAnalysisStrategy.java
│   ├── FootballAnalysisStrategy.java
│   ├── VolleyballAnalysisStrategy.java
│   └── GeneralAnalysisStrategy.java
│
├── observer
│   ├── MatchObserver.java
│   ├── ScoreBoardObserver.java
│   ├── StatisticsObserver.java
│   ├── RiskWarningObserver.java
│   └── ReportObserver.java
│
├── agent
│   ├── AiAgent.java
│   ├── DataCollectAgent.java
│   ├── SituationAnalysisAgent.java
│   ├── ReviewReportAgent.java
│   ├── LocalRuleAgent.java
│   └── RemoteModelAgent.java
│
├── repository
│   └── MatchRepository.java
│
└── Main.java
```

---

## 九、核心实体设计

### 9.1 SportType

```java
public enum SportType {
    BASKETBALL,
    FOOTBALL,
    VOLLEYBALL,
    GENERAL
}
```

### 9.2 EventType

```java
public enum EventType {
    SCORE,
    FOUL,
    ASSIST,
    REBOUND,
    STEAL,
    TURNOVER,
    TIMEOUT,
    YELLOW_CARD,
    RED_CARD,
    SUBSTITUTION,
    BLOCK,
    SERVE_ACE,
    ERROR
}
```

### 9.3 Match

`Match` 是通用比赛类，不直接绑定篮球、足球或排球。

核心字段：

```text
matchId
matchName
sportType
homeTeam
awayTeam
status
events
statistics
```

### 9.4 Team

`Team` 是通用队伍类。

核心字段：

```text
teamId
teamName
players
score
```

### 9.5 Player

`Player` 是通用球员类。

核心字段：

```text
playerId
playerName
teamId
number
statistics
```

### 9.6 MatchEvent

`MatchEvent` 是通用比赛事件类。

核心字段：

```text
eventId
matchId
teamId
playerId
eventType
scoreValue
eventTime
description
```

### 9.7 MatchStatistics

`MatchStatistics` 是比赛统计类。

核心字段：

```text
homeScore
awayScore
playerStats
teamStats
eventCount
leadingTeam
scoreDifference
```

---

## 十、数据库表结构设计

### 10.1 数据库选型

本项目使用 **MySQL 8.0** 作为关系型数据库，通过 **Spring Data JPA** 实现 ORM 映射，实体类即为第九节中的通用赛事对象。

### 10.2 表结构总览

```text
t_match（比赛表）
  ├── home_team_id → t_team（主队）
  ├── away_team_id → t_team（客队）
  └── t_match_event（比赛事件）
        ├── team_id → t_team
        └── player_id → t_player
              └── t_player_statistics（球员统计）
                    ├── player_id → t_player
                    └── match_id → t_match
```

### 10.3 t_match 比赛表

```sql
CREATE TABLE t_match (
    match_id      VARCHAR(36)  PRIMARY KEY,
    match_name    VARCHAR(100) NOT NULL,
    sport_type    VARCHAR(20)  NOT NULL COMMENT 'BASKETBALL/FOOTBALL/VOLLEYBALL/GENERAL',
    status        VARCHAR(20)  NOT NULL DEFAULT 'NOT_STARTED' COMMENT 'NOT_STARTED/RUNNING/PAUSED/FINISHED',
    home_team_id  VARCHAR(36),
    away_team_id  VARCHAR(36),
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 10.4 t_team 队伍表

```sql
CREATE TABLE t_team (
    team_id    VARCHAR(36)  PRIMARY KEY,
    team_name  VARCHAR(100) NOT NULL,
    score      INT          NOT NULL DEFAULT 0
);
```

### 10.5 t_player 球员表

```sql
CREATE TABLE t_player (
    player_id   VARCHAR(36)  PRIMARY KEY,
    player_name VARCHAR(100) NOT NULL,
    team_id     VARCHAR(36)  NOT NULL,
    number      INT          NOT NULL COMMENT '球衣号码',
    FOREIGN KEY (team_id) REFERENCES t_team(team_id)
);
```

### 10.6 t_match_event 比赛事件表

```sql
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
);
```

### 10.7 t_player_statistics 球员统计表

球员统计采用 **Key-Value 结构**，以适配不同赛事的统计维度差异（篮球有篮板、助攻；足球有红黄牌；排球有拦网、发球得分等）。

```sql
CREATE TABLE t_player_statistics (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    player_id  VARCHAR(36)  NOT NULL,
    match_id   VARCHAR(36)  NOT NULL,
    stat_key   VARCHAR(30)  NOT NULL COMMENT '统计维度：SCORE/FOUL/ASSIST/REBOUND/STEAL等',
    stat_value INT          NOT NULL DEFAULT 0,
    FOREIGN KEY (player_id) REFERENCES t_player(player_id),
    FOREIGN KEY (match_id)  REFERENCES t_match(match_id),
    UNIQUE KEY uk_player_match_stat (player_id, match_id, stat_key)
);
```

### 10.8 ER 关系图

```text
┌──────────────────────┐         ┌──────────────────────┐
│       t_match        │         │       t_team         │
├──────────────────────┤         ├──────────────────────┤
│ PK match_id          │         │ PK team_id           │
│    match_name        │         │    team_name         │
│    sport_type        │         │    score             │
│    status            │         └──────────┬───────────┘
│ FK home_team_id ─────┼────────→           │
│ FK away_team_id ─────┼────────→           │
│    create_time       │         ┌──────────┴───────────┐
│    update_time       │         │      t_player        │
└──────────┬───────────┘         ├──────────────────────┤
           │                     │ PK player_id         │
           │                     │    player_name       │
           │                     │ FK team_id ──────────┤
           │                     │    number            │
           │                     └──────────┬───────────┘
           │                                │
┌──────────┴───────────┐         ┌──────────┴───────────┐
│   t_match_event      │         │ t_player_statistics  │
├──────────────────────┤         ├──────────────────────┤
│ PK event_id          │         │ PK id (AUTO_INC)     │
│ FK match_id ─────────┤         │ FK player_id ────────┤
│ FK team_id ──────────┤         │ FK match_id ─────────┤
│ FK player_id ────────┤         │    stat_key          │
│    event_type        │         │    stat_value        │
│    score_value       │         └──────────────────────┘
│    event_time        │
│    description       │
│    create_time       │
└──────────────────────┘
```

---

## 十一、设计模式详细实现

## 11.1 状态模式

### 11.1.1 使用位置

状态模式用于比赛状态管理。

比赛状态包括：

1. 未开始；
2. 进行中；
3. 暂停中；
4. 已结束。

### 11.1.2 解决的问题

如果不使用状态模式，比赛状态判断会散落在业务代码中，例如：

```java
if (status == NOT_STARTED) { ... }
else if (status == RUNNING) { ... }
else if (status == PAUSED) { ... }
else if (status == FINISHED) { ... }
```

随着功能增加，代码会变得混乱。状态模式将不同状态下的行为封装到独立类中，可以避免比赛状态错乱。

### 11.1.3 状态行为设计

| 状态 | 允许操作 | 禁止操作 |
|---|---|---|
| 未开始 | 修改队伍、添加球员、开始比赛 | 录入得分事件 |
| 进行中 | 录入事件、暂停比赛、结束比赛 | 修改核心队伍信息 |
| 暂停中 | 查看统计、恢复比赛、结束比赛 | 录入比赛事件 |
| 已结束 | 查看报告、生成复盘 | 继续录入事件 |

---

## 11.2 责任链模式

### 11.2.1 使用位置

责任链模式用于比赛事件录入前的数据校验。

### 11.2.2 校验流程

```text
基础数据校验
  ↓
比赛状态校验
  ↓
球员合法性校验
  ↓
事件类型校验
  ↓
比分合法性校验
```

### 11.2.3 校验规则示例

1. 比赛必须存在；
2. 当前状态必须允许录入事件；
3. 球员必须属于当前比赛队伍；
4. 事件类型必须符合当前赛事；
5. 得分不能为负数；
6. 篮球得分只能是 1、2、3；
7. 足球进球得分只能是 1；
8. 已结束比赛不能继续录入事件。

### 11.2.4 解决的问题

责任链模式将复杂校验逻辑拆分为多个处理器，避免把所有校验都写在一个方法中，提升可维护性和扩展性。

---

## 11.3 策略模式

### 11.3.1 使用位置

策略模式用于不同赛事的态势分析。

### 11.3.2 策略类设计

```text
AnalysisStrategy
├── BasketballAnalysisStrategy
├── FootballAnalysisStrategy
├── VolleyballAnalysisStrategy
└── GeneralAnalysisStrategy
```

### 11.3.3 各赛事策略职责

#### 篮球分析策略

分析指标：

1. 比分；
2. 分差；
3. 连续得分；
4. 篮板；
5. 助攻；
6. 犯规；
7. 失误；
8. 关键球员表现。

#### 足球分析策略

分析指标：

1. 进球数；
2. 红黄牌；
3. 犯规；
4. 换人；
5. 当前领先方。

#### 排球分析策略

分析指标：

1. 比分；
2. 发球得分；
3. 拦网；
4. 失误；
5. 暂停。

#### 通用赛事策略

分析指标：

1. 当前比分；
2. 当前领先方；
3. 分差；
4. 关键事件数量。

### 11.3.4 解决的问题

策略模式让系统可以根据 `SportType` 动态选择不同赛事分析规则。后续新增赛事时，只需要新增一个策略类，不需要修改核心业务代码。

---

## 11.4 观察者模式

### 11.4.1 使用位置

观察者模式用于比赛事件发生后的模块联动更新。

### 11.4.2 观察者设计

```text
MatchObserver
├── ScoreBoardObserver
├── StatisticsObserver
├── RiskWarningObserver
└── ReportObserver
```

### 11.4.3 事件联动流程

```text
录入比赛事件
  ↓
责任链校验
  ↓
更新比赛数据
  ↓
通知观察者
  ↓
比分板更新
  ↓
统计数据更新
  ↓
风险预警更新
  ↓
复盘记录更新
```

### 11.4.4 解决的问题

观察者模式可以避免业务模块之间直接调用，使比分板、统计、预警、复盘等模块在事件变化后自动更新，降低模块耦合。

---

## 十二、AI Agent 模块设计

### 12.1 AI Agent 模块定位

本项目中的 AI Agent 不是指开发过程中使用的聊天工具，而是系统内部的业务模块。

AI Agent 模块负责：

1. 整理赛事数据；
2. 生成实时态势分析；
3. 生成赛后复盘；
4. 输出报告文本。

### 12.2 Agent 接口设计

```java
public interface AiAgent {
    String execute(Match match);
}
```

### 12.3 Agent 类型设计

```text
DataCollectAgent
SituationAnalysisAgent
ReviewReportAgent
LocalRuleAgent
RemoteModelAgent
```

### 12.4 各 Agent 职责

#### DataCollectAgent

负责将原始比赛事件整理为结构化数据，为后续统计和分析做准备。

#### SituationAnalysisAgent

负责根据当前比赛数据生成实时态势分析。

#### ReviewReportAgent

负责在比赛结束后生成赛后复盘报告。

#### LocalRuleAgent

使用本地规则模板模拟 AI 输出，适合课程设计基础版本。

#### RemoteModelAgent

预留真实大模型 API 调用入口，后续可以替换 `LocalRuleAgent`。

### 12.5 是否必须调用真实大模型 API

需要

### 12.6 Agent 调用流程

```text
比赛事件数据
  ↓
业务服务层整理
  ↓
DataCollectAgent 结构化处理
  ↓
SituationAnalysisAgent 生成态势分析
  ↓
ReviewReportAgent 生成赛后复盘
  ↓
ReportService 输出报告
```

---

## 十三、系统核心业务流程

### 13.1 比赛创建流程

```text
用户创建比赛
  ↓
选择赛事类型
  ↓
添加主队和客队
  ↓
添加球员
  ↓
保存比赛信息
```

### 13.2 事件录入流程

```text
用户录入比赛事件
  ↓
EventService 接收事件
  ↓
责任链校验事件
  ↓
状态模式判断是否允许操作
  ↓
保存事件
  ↓
更新比赛统计
  ↓
通知观察者
  ↓
生成实时态势分析
```

### 13.3 态势分析流程

```text
获取当前比赛数据
  ↓
根据 SportType 选择分析策略
  ↓
生成基础分析结果
  ↓
SituationAnalysisAgent 组织分析文本
  ↓
返回分析结果
```

### 13.4 赛后复盘流程

```text
比赛结束
  ↓
状态切换为已结束
  ↓
汇总比赛数据
  ↓
ReviewReportAgent 生成复盘报告
  ↓
ReportService 输出完整报告
```

---

## 十四、最小可完成版本

为了保证项目可完成，本项目建议先实现最小可运行版本。

### 14.1 必做功能

1. 创建比赛；
2. 选择赛事类型；
3. 添加两支队伍；
4. 添加球员；
5. 开始比赛；
6. 录入比赛事件；
7. 通过责任链校验事件；
8. 自动更新比分；
9. 通过观察者更新统计；
10. 通过策略模式生成态势分析；
11. 结束比赛；
12. 生成赛后复盘。

### 14.2 篮球完整实现

篮球完整实现以下事件：

1. 得分；
2. 犯规；
3. 助攻；
4. 篮板；
5. 暂停。

### 14.3 足球轻量实现

足球轻量实现以下事件：

1. 进球；
2. 犯规；
3. 黄牌；
4. 红牌；
5. 换人。

### 14.4 排球轻量实现

排球轻量实现以下事件：

1. 得分；
2. 拦网；
3. 发球得分；
4. 失误；
5. 暂停。

---

## 项目总结

MatchLens 体育赛事数据智能统计与态势分析系统采用通用赛事模型作为核心基础，避免将系统写死为篮球专用系统。系统以 MySQL 作为持久化存储，通过 Spring Data JPA 实现 ORM 映射，包含比赛、队伍、球员、事件、球员统计 5 张核心表。系统以篮球作为完整演示赛事，实现比赛创建、事件录入、数据统计、态势分析和赛后复盘；同时通过足球和排球的轻量策略适配，体现多赛事扩展能力。

系统通过状态模式管理比赛状态，避免比赛状态错乱；通过责任链模式校验比赛事件，避免统计规则混乱；通过策略模式适配不同赛事分析规则，实现多赛事扩展；通过观察者模式实现比赛事件发生后的比分、统计、预警和复盘联动更新。

AI Agent 模块被设计为独立业务封装层，用于赛事数据整理、态势分析和赛后复盘。当前版本可采用本地规则模板模拟 AI 输出，后续可以替换为真实大模型 API 调用。该设计符合课程设计中“AI 模块定位为纯业务封装与能力调用”的要求，也体现了设计模式对 Vibe Coding 增量开发过程的架构约束作用。
