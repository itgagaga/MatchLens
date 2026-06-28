# MatchLens 前端设计与实现说明

## 1. 文档目的

本文用于说明 **MatchLens 体育赛事数据智能统计与态势分析系统** 的前端设计与实现方案。项目后端已经提供比赛管理、队伍球员配置、比赛状态控制、赛事事件录入、统计查询、实时态势分析和赛后复盘等接口。前端采用 **HTML + CSS + JavaScript** 实现，不引入 Vue、React、Angular 等前端框架，直接放置在 Spring Boot 项目的：

```text
src/main/resources/static
```

目录下，由 Spring Boot 静态资源机制直接访问。

前端实现目标不是做复杂商业化页面，而是为课程设计和答辩演示提供一个清晰、可运行、能体现系统架构能力的 Web 操作界面。

---

## 2. 项目后端现状梳理

### 2.1 后端核心模块

根据当前项目代码，后端主要包含以下模块：

| 模块 | 包或类 | 作用 |
|---|---|---|
| 比赛管理 | `MatchController`、`MatchService` | 创建比赛、查看比赛、设置队伍、添加球员、控制比赛状态 |
| 事件录入 | `EventController`、`EventService` | 录入得分、犯规、助攻、红黄牌、拦网等赛事事件 |
| 统计分析 | `StatisticsService` | 获取比赛统计信息和实时态势分析 |
| 报告生成 | `ReportController`、`ReportService` | 生成比赛统计、态势分析和赛后复盘报告 |
| 状态模式 | `state` 包 | 管理未开始、进行中、暂停、已结束等状态 |
| 责任链模式 | `chain` 包 | 对事件录入进行比赛存在性、状态、球员、事件类型、得分规则等校验 |
| 策略模式 | `strategy` 包 | 根据篮球、足球、排球、通用赛事生成不同分析结果 |
| 观察者模式 | `observer` 包 | 事件录入后自动触发比分、统计、预警、复盘素材更新 |
| AI Agent | `agent` 包 | 封装数据采集、实时态势分析、赛后复盘、本地降级和远程模型调用 |

### 2.2 当前已暴露接口

#### 比赛管理接口

| 功能 | 方法 | 地址 | 请求体 |
|---|---|---|---|
| 创建比赛 | `POST` | `/api/matches` | `{ "matchName": "...", "sportType": "BASKETBALL" }` |
| 获取全部比赛 | `GET` | `/api/matches` | 无 |
| 获取单场比赛 | `GET` | `/api/matches/{id}` | 无 |
| 设置主客队 | `POST` | `/api/matches/{id}/teams` | `{ "homeName": "...", "awayName": "..." }` |
| 添加球员 | `POST` | `/api/matches/{id}/players` | `{ "home": true, "playerName": "...", "number": 23 }` |
| 开始比赛 | `POST` | `/api/matches/{id}/start` | 无 |
| 暂停比赛 | `POST` | `/api/matches/{id}/pause` | 无 |
| 恢复比赛 | `POST` | `/api/matches/{id}/resume` | 无 |
| 结束比赛 | `POST` | `/api/matches/{id}/finish` | 无 |

#### 赛事事件接口

| 功能 | 方法 | 地址 | 请求体 |
|---|---|---|---|
| 录入赛事事件 | `POST` | `/api/matches/{matchId}/events` | `{ "teamId": "...", "playerId": "...", "eventType": "SCORE", "scoreValue": 2, "description": "..." }` |

#### 统计、分析和报告接口

| 功能 | 方法 | 地址 | 返回 |
|---|---|---|---|
| 获取统计信息 | `GET` | `/api/matches/{matchId}/statistics` | `MatchStatistics` JSON |
| 获取实时态势分析 | `GET` | `/api/matches/{matchId}/analysis` | 文本 |
| 生成赛后复盘报告 | `GET` | `/api/matches/{matchId}/report` | 文本 |

---

## 3. 前端总体设计

### 3.1 实现方式

前端采用单页面应用式设计，但不使用复杂路由框架。页面由一个 `index.html` 承载，使用原生 JavaScript 维护当前选中的比赛和页面状态。

访问路径：

```text
http://localhost:8080/
```

或：

```text
http://localhost:8080/index.html
```

Spring Boot 默认会把 `src/main/resources/static/index.html` 作为静态首页。

### 3.2 前端目录结构

建议在项目中新增以下目录和文件：

```text
src/main/resources/static/
├── index.html
├── css/
│   └── style.css
└── js/
    ├── constants.js
    ├── api.js
    ├── render.js
    └── app.js
```

各文件职责如下：

| 文件 | 职责 |
|---|---|
| `index.html` | 页面结构，包括比赛列表、表单、比分面板、事件录入、分析报告区域 |
| `css/style.css` | 页面布局、卡片样式、比分板、按钮、表单、提示信息样式 |
| `js/constants.js` | 赛事类型、事件类型、状态中文映射等常量 |
| `js/api.js` | 统一封装 `fetch` 请求 |
| `js/render.js` | 负责把后端数据渲染到页面 |
| `js/app.js` | 页面初始化、事件绑定、用户操作流程控制 |

如果希望进一步简化，也可以只保留：

```text
index.html
css/style.css
js/app.js
```

但为了报告和答辩更好说明，推荐拆分为四个 JS 文件。

---

## 4. 页面功能设计

### 4.1 页面整体布局

页面采用「左侧比赛列表 + 右侧操作面板」的结构：

```text
┌──────────────────────────────────────────────┐
│ 顶部导航：MatchLens 体育赛事态势分析系统      │
├───────────────┬──────────────────────────────┤
│ 左侧区域       │ 右侧主区域                    │
│ - 创建比赛     │ - 比赛概览与比分板              │
│ - 比赛列表     │ - 队伍与球员配置                │
│               │ - 比赛状态控制                  │
│               │ - 赛事事件录入                  │
│               │ - 实时统计                      │
│               │ - 态势分析                      │
│               │ - 赛后复盘报告                  │
└───────────────┴──────────────────────────────┘
```

### 4.2 首页顶部区域

顶部显示系统名称和当前选中比赛状态：

```text
MatchLens 体育赛事数据智能统计与态势分析系统
当前比赛：湖人 vs 勇士
状态：进行中
赛事：篮球
```

顶部右侧可以放置刷新按钮：

```text
刷新比赛列表
刷新当前比赛
```

### 4.3 创建比赛区域

创建比赛表单包含：

| 字段 | 类型 | 示例 |
|---|---|---|
| 比赛名称 | 文本输入框 | 湖人 vs 勇士 |
| 赛事类型 | 下拉框 | 篮球、足球、排球、通用赛事 |
| 创建按钮 | 按钮 | 创建比赛 |

对应后端接口：

```http
POST /api/matches
```

请求示例：

```json
{
  "matchName": "湖人 vs 勇士",
  "sportType": "BASKETBALL"
}
```

前端创建成功后应自动刷新比赛列表，并选中新创建的比赛。

### 4.4 比赛列表区域

比赛列表显示：

| 字段 | 说明 |
|---|---|
| 比赛名称 | `matchName` |
| 赛事类型 | `sportType` |
| 状态 | `status` |
| 创建时间 | `createTime` |

点击某场比赛后，前端调用：

```http
GET /api/matches/{id}
```

并刷新右侧所有详情区域。

### 4.5 比赛概览与比分板

比分板展示主队、客队和比分：

```text
湖人  82 : 76  勇士
```

同时展示：

```text
比赛状态：RUNNING
赛事类型：BASKETBALL
事件总数：12
领先方：湖人
分差：6
```

如果比赛尚未设置队伍，则显示：

```text
请先设置主队和客队
```

### 4.6 队伍与球员配置区域

队伍配置表单：

| 字段 | 类型 |
|---|---|
| 主队名称 | 输入框 |
| 客队名称 | 输入框 |
| 设置队伍 | 按钮 |

对应接口：

```http
POST /api/matches/{id}/teams
```

球员添加表单：

| 字段 | 类型 |
|---|---|
| 队伍 | 主队/客队单选或下拉框 |
| 球员姓名 | 输入框 |
| 球衣号码 | 数字输入框 |
| 添加球员 | 按钮 |

对应接口：

```http
POST /api/matches/{id}/players
```

请求示例：

```json
{
  "home": true,
  "playerName": "张三",
  "number": 23
}
```

页面中应展示双方球员列表：

```text
主队球员
#23 张三
#11 李四

客队球员
#30 王五
#7 赵六
```

### 4.7 比赛状态控制区域

状态控制按钮：

```text
开始比赛
暂停比赛
恢复比赛
结束比赛
```

对应接口：

```http
POST /api/matches/{id}/start
POST /api/matches/{id}/pause
POST /api/matches/{id}/resume
POST /api/matches/{id}/finish
```

前端可以根据比赛状态控制按钮是否可用：

| 当前状态 | 可用按钮 |
|---|---|
| `NOT_STARTED` | 开始比赛 |
| `RUNNING` | 暂停比赛、结束比赛 |
| `PAUSED` | 恢复比赛、结束比赛 |
| `FINISHED` | 不允许再控制状态 |

这里可以很好地体现后端状态模式。前端只做基础按钮控制，真正的状态合法性仍以后端返回结果为准。

### 4.8 赛事事件录入区域

事件录入表单字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| 队伍 | 下拉框 | 主队或客队 |
| 球员 | 下拉框 | 根据所选队伍动态切换 |
| 事件类型 | 下拉框 | 根据赛事类型动态展示 |
| 得分值 | 数字输入框 | 得分事件填写，非得分事件可为 0 |
| 描述 | 文本输入框 | 例如“三分命中”“防守犯规” |
| 提交事件 | 按钮 | 调用事件录入接口 |

对应接口：

```http
POST /api/matches/{matchId}/events
```

请求示例：

```json
{
  "teamId": "team-home-id",
  "playerId": "player-id",
  "eventType": "SCORE",
  "scoreValue": 2,
  "description": "中距离投篮命中"
}
```

事件类型建议按赛事类型动态过滤：

| 赛事类型 | 可选事件 |
|---|---|
| 篮球 | `SCORE`、`FOUL`、`ASSIST`、`REBOUND`、`STEAL`、`TURNOVER`、`TIMEOUT` |
| 足球 | `SCORE`、`FOUL`、`YELLOW_CARD`、`RED_CARD`、`SUBSTITUTION` |
| 排球 | `SCORE`、`BLOCK`、`SERVE_ACE`、`ERROR` |
| 通用赛事 | `SCORE`、`FOUL` |

录入事件成功后，页面应自动刷新：

1. 比分板；
2. 球员统计；
3. 事件时间线；
4. 实时统计；
5. 态势分析。

这样可以在演示中体现观察者模式：一次事件录入后，比分、统计、预警和分析素材自动联动更新。

### 4.9 实时统计区域

调用接口：

```http
GET /api/matches/{matchId}/statistics
```

展示内容：

| 字段 | 显示方式 |
|---|---|
| 主队得分 | 大号数字 |
| 客队得分 | 大号数字 |
| 事件总数 | 数字 |
| 领先方 | 文本 |
| 分差 | 数字 |
| 球队统计 | 表格 |
| 球员统计 | 表格 |

由于当前后端 `MatchStatistics` 的 `playerStats` 是以 `playerId` 为 key，前端可以结合 `currentMatch.homeTeam.players` 和 `currentMatch.awayTeam.players` 显示球员姓名。

### 4.10 实时态势分析区域

调用接口：

```http
GET /api/matches/{matchId}/analysis
```

该接口当前返回文本，因此前端使用：

```html
<pre id="analysisText"></pre>
```

展示即可。

按钮设计：

```text
生成/刷新态势分析
```

如果后续后端把实时态势分析改为远程大模型主路径，前端不用大改，只需要继续调用同一个接口。

### 4.11 赛后复盘报告区域

调用接口：

```http
GET /api/matches/{matchId}/report
```

该接口当前返回文本，前端同样使用：

```html
<pre id="reportText"></pre>
```

展示。

按钮设计：

```text
生成赛后复盘
```

建议在比赛状态为 `FINISHED` 时高亮该按钮。虽然前端可以控制按钮状态，但最终仍应以后端校验为准。

---

## 5. 前端与设计模式展示关系

前端不是直接实现设计模式，而是通过页面操作把后端设计模式的效果展示出来。

| 后端模式 | 前端展示方式 |
|---|---|
| 状态模式 | 通过开始、暂停、恢复、结束按钮展示状态流转；暂停或结束后尝试录入事件，前端显示后端拒绝信息 |
| 责任链模式 | 录入非法事件，例如篮球 4 分事件、不属于队伍的球员、暂停时录入事件，页面展示校验失败提示 |
| 策略模式 | 创建篮球、足球、排球不同比赛，事件类型和态势分析结果不同 |
| 观察者模式 | 成功录入一次事件后，比分板、统计面板、事件列表、分析素材自动刷新 |
| AI Agent | 点击态势分析和赛后复盘按钮，展示 AI Agent 生成的文本结果；后续可展示 AI 调用日志 |

---

## 6. 关键前端代码设计

### 6.1 `constants.js`

```javascript
const SPORT_TYPES = [
  { value: 'BASKETBALL', label: '篮球' },
  { value: 'FOOTBALL', label: '足球' },
  { value: 'VOLLEYBALL', label: '排球' },
  { value: 'GENERAL', label: '通用赛事' }
];

const STATUS_TEXT = {
  NOT_STARTED: '未开始',
  RUNNING: '进行中',
  PAUSED: '暂停中',
  FINISHED: '已结束'
};

const EVENT_TYPES_BY_SPORT = {
  BASKETBALL: [
    ['SCORE', '得分'],
    ['FOUL', '犯规'],
    ['ASSIST', '助攻'],
    ['REBOUND', '篮板'],
    ['STEAL', '抢断'],
    ['TURNOVER', '失误'],
    ['TIMEOUT', '暂停']
  ],
  FOOTBALL: [
    ['SCORE', '进球'],
    ['FOUL', '犯规'],
    ['YELLOW_CARD', '黄牌'],
    ['RED_CARD', '红牌'],
    ['SUBSTITUTION', '换人']
  ],
  VOLLEYBALL: [
    ['SCORE', '得分'],
    ['BLOCK', '拦网'],
    ['SERVE_ACE', '发球直接得分'],
    ['ERROR', '失误']
  ],
  GENERAL: [
    ['SCORE', '得分'],
    ['FOUL', '犯规']
  ]
};
```

### 6.2 `api.js`

```javascript
const api = {
  async request(url, options = {}, responseType = 'json') {
    const config = {
      headers: { 'Content-Type': 'application/json' },
      ...options
    };

    const response = await fetch(url, config);

    if (!response.ok) {
      throw new Error(`请求失败：${response.status}`);
    }

    if (responseType === 'text') {
      return response.text();
    }

    return response.json();
  },

  getMatches() {
    return this.request('/api/matches');
  },

  getMatch(id) {
    return this.request(`/api/matches/${id}`);
  },

  createMatch(data) {
    return this.request('/api/matches', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  setTeams(matchId, data) {
    return this.request(`/api/matches/${matchId}/teams`, {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  addPlayer(matchId, data) {
    return this.request(`/api/matches/${matchId}/players`, {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  changeStatus(matchId, action) {
    return this.request(`/api/matches/${matchId}/${action}`, {
      method: 'POST'
    });
  },

  recordEvent(matchId, data) {
    return this.request(`/api/matches/${matchId}/events`, {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  getStatistics(matchId) {
    return this.request(`/api/matches/${matchId}/statistics`);
  },

  getAnalysis(matchId) {
    return this.request(`/api/matches/${matchId}/analysis`, {}, 'text');
  },

  getReport(matchId) {
    return this.request(`/api/matches/${matchId}/report`, {}, 'text');
  }
};
```

### 6.3 `app.js` 核心状态

```javascript
const state = {
  matches: [],
  currentMatch: null,
  statistics: null,
  analysis: '',
  report: ''
};

document.addEventListener('DOMContentLoaded', () => {
  bindEvents();
  loadMatches();
});
```

### 6.4 页面初始化流程

```javascript
async function loadMatches() {
  try {
    state.matches = await api.getMatches();
    renderMatchList();

    if (!state.currentMatch && state.matches.length > 0) {
      await selectMatch(state.matches[0].matchId);
    }
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function selectMatch(matchId) {
  try {
    state.currentMatch = await api.getMatch(matchId);
    await refreshStatistics();
    renderAll();
  } catch (error) {
    showToast(error.message, 'error');
  }
}
```

### 6.5 创建比赛

```javascript
async function handleCreateMatch(event) {
  event.preventDefault();

  const matchName = document.querySelector('#matchName').value.trim();
  const sportType = document.querySelector('#sportType').value;

  if (!matchName) {
    showToast('请输入比赛名称', 'warning');
    return;
  }

  const match = await api.createMatch({ matchName, sportType });
  showToast('比赛创建成功', 'success');
  await loadMatches();
  await selectMatch(match.matchId);
}
```

### 6.6 设置队伍

```javascript
async function handleSetTeams(event) {
  event.preventDefault();

  if (!state.currentMatch) {
    showToast('请先选择比赛', 'warning');
    return;
  }

  const homeName = document.querySelector('#homeName').value.trim();
  const awayName = document.querySelector('#awayName').value.trim();

  const result = await api.setTeams(state.currentMatch.matchId, { homeName, awayName });
  showResult(result);
  await selectMatch(state.currentMatch.matchId);
}
```

### 6.7 添加球员

```javascript
async function handleAddPlayer(event) {
  event.preventDefault();

  const home = document.querySelector('#playerTeam').value === 'home';
  const playerName = document.querySelector('#playerName').value.trim();
  const number = Number(document.querySelector('#playerNumber').value);

  const result = await api.addPlayer(state.currentMatch.matchId, {
    home,
    playerName,
    number
  });

  showResult(result);
  await selectMatch(state.currentMatch.matchId);
}
```

### 6.8 录入事件

```javascript
async function handleRecordEvent(event) {
  event.preventDefault();

  const teamId = document.querySelector('#eventTeam').value;
  const playerId = document.querySelector('#eventPlayer').value;
  const eventType = document.querySelector('#eventType').value;
  const scoreValue = Number(document.querySelector('#scoreValue').value || 0);
  const description = document.querySelector('#eventDescription').value.trim();

  const result = await api.recordEvent(state.currentMatch.matchId, {
    teamId,
    playerId,
    eventType,
    scoreValue,
    description
  });

  showResult(result);

  if (result.success) {
    await selectMatch(state.currentMatch.matchId);
    await refreshAnalysis();
  }
}
```

### 6.9 获取态势分析和报告

```javascript
async function refreshAnalysis() {
  if (!state.currentMatch) return;

  state.analysis = await api.getAnalysis(state.currentMatch.matchId);
  renderAnalysis();
}

async function generateReport() {
  if (!state.currentMatch) return;

  state.report = await api.getReport(state.currentMatch.matchId);
  renderReport();
}
```

---

## 7. `index.html` 页面结构建议

`index.html` 可以按以下结构组织：

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <title>MatchLens 体育赛事态势分析系统</title>
  <link rel="stylesheet" href="./css/style.css" />
</head>
<body>
  <header class="app-header">
    <div>
      <h1>MatchLens 体育赛事数据智能统计与态势分析系统</h1>
      <p>多赛事统计 · 设计模式驱动 · AI Agent 态势分析</p>
    </div>
    <button id="refreshBtn">刷新</button>
  </header>

  <main class="layout">
    <aside class="sidebar">
      <section class="card">
        <h2>创建比赛</h2>
        <form id="createMatchForm">
          <input id="matchName" placeholder="比赛名称，例如 湖人 vs 勇士" />
          <select id="sportType"></select>
          <button type="submit">创建比赛</button>
        </form>
      </section>

      <section class="card">
        <h2>比赛列表</h2>
        <div id="matchList" class="match-list"></div>
      </section>
    </aside>

    <section class="content">
      <section class="card">
        <h2>比赛概览</h2>
        <div id="matchOverview"></div>
      </section>

      <section class="card">
        <h2>队伍与球员配置</h2>
        <form id="setTeamsForm"></form>
        <form id="addPlayerForm"></form>
        <div id="playerList"></div>
      </section>

      <section class="card">
        <h2>比赛状态控制</h2>
        <div id="statusActions"></div>
      </section>

      <section class="card">
        <h2>赛事事件录入</h2>
        <form id="recordEventForm"></form>
        <div id="eventTimeline"></div>
      </section>

      <section class="card">
        <h2>实时统计</h2>
        <div id="statisticsPanel"></div>
      </section>

      <section class="card">
        <h2>实时态势分析</h2>
        <button id="analysisBtn">生成/刷新态势分析</button>
        <pre id="analysisText"></pre>
      </section>

      <section class="card">
        <h2>赛后复盘报告</h2>
        <button id="reportBtn">生成赛后复盘</button>
        <pre id="reportText"></pre>
      </section>
    </section>
  </main>

  <div id="toast" class="toast"></div>

  <script src="./js/constants.js"></script>
  <script src="./js/api.js"></script>
  <script src="./js/render.js"></script>
  <script src="./js/app.js"></script>
</body>
</html>
```

---

## 8. CSS 设计建议

页面风格建议采用简洁的深浅卡片风格，重点突出比分、状态、分析结果。

### 8.1 布局样式

```css
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: "Microsoft YaHei", Arial, sans-serif;
  background: #f5f7fb;
  color: #1f2937;
}

.app-header {
  height: 88px;
  padding: 16px 28px;
  background: #111827;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.app-header h1 {
  margin: 0;
  font-size: 24px;
}

.app-header p {
  margin: 6px 0 0;
  color: #cbd5e1;
}

.layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 18px;
  padding: 18px;
}

.sidebar,
.content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  background: #fff;
  border-radius: 14px;
  padding: 18px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.card h2 {
  margin-top: 0;
  font-size: 18px;
}
```

### 8.2 表单和按钮样式

```css
input,
select,
textarea {
  width: 100%;
  padding: 10px 12px;
  margin-bottom: 10px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  outline: none;
}

button {
  border: none;
  border-radius: 8px;
  padding: 10px 14px;
  background: #2563eb;
  color: #fff;
  cursor: pointer;
}

button:hover {
  background: #1d4ed8;
}

button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}
```

### 8.3 比分板样式

```css
.scoreboard {
  display: grid;
  grid-template-columns: 1fr 120px 1fr;
  align-items: center;
  text-align: center;
  gap: 16px;
}

.team-name {
  font-size: 22px;
  font-weight: 700;
}

.score {
  font-size: 36px;
  font-weight: 800;
  color: #dc2626;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  background: #e0f2fe;
  color: #0369a1;
  font-size: 13px;
}
```

### 8.4 文本报告区域

```css
pre {
  white-space: pre-wrap;
  line-height: 1.7;
  background: #0f172a;
  color: #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  overflow-x: auto;
}

.toast {
  position: fixed;
  right: 24px;
  bottom: 24px;
  min-width: 220px;
  padding: 12px 16px;
  border-radius: 10px;
  background: #111827;
  color: #fff;
  display: none;
}

.toast.show {
  display: block;
}
```

---

## 9. 渲染逻辑设计

### 9.1 渲染比赛列表

```javascript
function renderMatchList() {
  const container = document.querySelector('#matchList');

  if (!state.matches.length) {
    container.innerHTML = '<p class="empty">暂无比赛，请先创建。</p>';
    return;
  }

  container.innerHTML = state.matches.map(match => `
    <div class="match-item ${state.currentMatch?.matchId === match.matchId ? 'active' : ''}"
         onclick="selectMatch('${match.matchId}')">
      <strong>${match.matchName}</strong>
      <span>${SPORT_TYPES.find(s => s.value === match.sportType)?.label || match.sportType}</span>
      <em>${STATUS_TEXT[match.status] || match.status}</em>
    </div>
  `).join('');
}
```

### 9.2 渲染比赛概览

```javascript
function renderOverview() {
  const match = state.currentMatch;
  const container = document.querySelector('#matchOverview');

  if (!match) {
    container.innerHTML = '<p class="empty">请选择比赛。</p>';
    return;
  }

  const home = match.homeTeam;
  const away = match.awayTeam;

  if (!home || !away) {
    container.innerHTML = `
      <p>比赛名称：${match.matchName}</p>
      <p>赛事类型：${match.sportType}</p>
      <p>比赛状态：<span class="status-badge">${STATUS_TEXT[match.status]}</span></p>
      <p class="empty">请先设置主队和客队。</p>
    `;
    return;
  }

  container.innerHTML = `
    <div class="scoreboard">
      <div class="team-name">${home.teamName}</div>
      <div class="score">${home.score} : ${away.score}</div>
      <div class="team-name">${away.teamName}</div>
    </div>
    <p>赛事类型：${match.sportType}</p>
    <p>比赛状态：<span class="status-badge">${STATUS_TEXT[match.status]}</span></p>
  `;
}
```

### 9.3 渲染事件时间线

```javascript
function renderEventTimeline() {
  const events = state.currentMatch?.events || [];
  const container = document.querySelector('#eventTimeline');

  if (!events.length) {
    container.innerHTML = '<p class="empty">暂无事件。</p>';
    return;
  }

  container.innerHTML = events.slice().reverse().map(event => `
    <div class="timeline-item">
      <strong>${event.eventType}</strong>
      <span>${event.scoreValue ? `+${event.scoreValue}` : ''}</span>
      <p>${event.description || '无描述'}</p>
      <small>${event.eventTime || ''}</small>
    </div>
  `).join('');
}
```

### 9.4 渲染统计信息

```javascript
function renderStatistics() {
  const stats = state.statistics;
  const container = document.querySelector('#statisticsPanel');

  if (!stats) {
    container.innerHTML = '<p class="empty">暂无统计数据。</p>';
    return;
  }

  container.innerHTML = `
    <div class="stats-grid">
      <div>主队得分：<strong>${stats.homeScore}</strong></div>
      <div>客队得分：<strong>${stats.awayScore}</strong></div>
      <div>事件总数：<strong>${stats.eventCount}</strong></div>
      <div>领先方：<strong>${stats.leadingTeam || '平局'}</strong></div>
      <div>分差：<strong>${stats.scoreDifference}</strong></div>
    </div>
  `;
}
```

---

## 10. 前端异常处理设计

### 10.1 统一提示函数

```javascript
function showToast(message, type = 'info') {
  const toast = document.querySelector('#toast');
  toast.textContent = message;
  toast.className = `toast show ${type}`;

  setTimeout(() => {
    toast.className = 'toast';
  }, 2400);
}

function showResult(result) {
  if (!result) return;

  if (result.success) {
    showToast(result.message || result.data || '操作成功', 'success');
  } else {
    showToast(result.message || '操作失败', 'error');
  }
}
```

### 10.2 后端校验失败展示

例如篮球录入 4 分事件，后端责任链会返回失败结果。前端不需要自己重复所有业务校验，只需要把后端返回的失败信息展示出来：

```javascript
const result = await api.recordEvent(matchId, data);

if (!result.success) {
  showToast(result.message, 'error');
  return;
}
```

这能体现责任链模式的效果。

---

## 11. 推荐实现步骤

### 第一步：创建静态目录

在项目中创建：

```text
src/main/resources/static/index.html
src/main/resources/static/css/style.css
src/main/resources/static/js/constants.js
src/main/resources/static/js/api.js
src/main/resources/static/js/render.js
src/main/resources/static/js/app.js
```

### 第二步：先实现比赛列表和创建比赛

先完成：

1. `GET /api/matches`
2. `POST /api/matches`
3. 比赛列表渲染
4. 点击比赛加载详情

这一步完成后，页面已经可以展示基本数据。

### 第三步：实现队伍和球员配置

完成：

1. 设置主客队；
2. 添加主队球员；
3. 添加客队球员；
4. 渲染双方球员列表。

### 第四步：实现状态控制

完成：

1. 开始比赛；
2. 暂停比赛；
3. 恢复比赛；
4. 结束比赛；
5. 按状态禁用不合理按钮。

### 第五步：实现事件录入

完成：

1. 根据赛事类型展示事件类型；
2. 根据队伍切换球员下拉框；
3. 提交事件；
4. 展示成功或失败提示；
5. 事件成功后刷新比分、统计、事件列表。

### 第六步：实现统计、态势分析和报告

完成：

1. 统计面板；
2. 实时态势分析文本展示；
3. 赛后复盘报告文本展示。

### 第七步：整理演示流程

准备一组固定演示数据：

1. 创建篮球比赛；
2. 设置两队；
3. 添加球员；
4. 开始比赛；
5. 录入合法得分；
6. 录入非法 4 分事件，展示责任链拒绝；
7. 暂停比赛后尝试录入事件，展示状态模式；
8. 恢复比赛继续录入；
9. 查看统计和态势分析；
10. 结束比赛；
11. 生成赛后复盘报告；
12. 创建足球或排球比赛，展示策略可替换。

---

## 12. 答辩展示说明

前端页面在答辩时主要承担“可视化演示后端架构”的作用：

| 演示操作 | 证明点 |
|---|---|
| 创建不同赛事类型 | 多赛事适配 |
| 篮球、足球、排球事件类型不同 | 策略模式 |
| 开始、暂停、恢复、结束比赛 | 状态模式 |
| 暂停时录入事件被拒绝 | 状态校验有效 |
| 篮球 4 分事件被拒绝 | 责任链和比分规则校验有效 |
| 合法事件录入后比分和统计自动刷新 | 观察者模式 |
| 点击态势分析 | AI Agent 或本地策略分析模块 |
| 点击赛后复盘 | 赛后智能复盘 Agent |
| 远程 AI 失败时仍能输出报告 | 本地降级机制 |

---

## 13. 当前前端实现需要注意的问题

### 13.1 后端部分接口返回纯文本

`/analysis` 和 `/report` 返回的是纯文本，不是 JSON。因此前端请求时必须使用：

```javascript
response.text()
```

不能直接使用：

```javascript
response.json()
```

否则会报解析错误。

### 13.2 Result 和普通对象混合返回

部分接口返回 `Result<String>`：

```json
{
  "success": true,
  "message": "success",
  "data": "比赛已开始"
}
```

而创建比赛和查询比赛返回的是 `Match` 对象。因此前端 `api.js` 要区分不同接口的返回类型。

### 13.3 前端不要替代后端业务校验

例如：

1. 是否允许暂停；
2. 是否允许录入事件；
3. 球员是否属于该比赛；
4. 篮球是否允许 4 分；
5. 结束后是否允许继续录入。

这些规则应该由后端状态模式和责任链模式判断。前端可以做基础体验优化，但不能把核心业务规则全部写死在前端。

### 13.4 无需处理跨域

因为前端文件放在：

```text
src/main/resources/static
```

并由同一个 Spring Boot 服务提供，所以请求 `/api/...` 属于同源请求，不需要配置 CORS。

---

## 14. 可直接给 AI 编程助手的前端实现指令

```text
请在当前 Spring Boot 项目的 src/main/resources/static 目录下，为 MatchLens 体育赛事数据智能统计与态势分析系统实现一个原生 HTML + CSS + JavaScript 前端页面。

要求：
1. 不使用 Vue、React、Angular，不使用构建工具。
2. 新增 index.html、css/style.css、js/constants.js、js/api.js、js/render.js、js/app.js。
3. 页面采用左侧比赛列表、右侧操作面板布局。
4. 支持创建比赛、查看比赛列表、选择比赛、设置主客队、添加球员、开始/暂停/恢复/结束比赛。
5. 支持录入赛事事件，事件字段包括 teamId、playerId、eventType、scoreValue、description。
6. 事件类型根据 sportType 动态变化：
   - 篮球：SCORE、FOUL、ASSIST、REBOUND、STEAL、TURNOVER、TIMEOUT；
   - 足球：SCORE、FOUL、YELLOW_CARD、RED_CARD、SUBSTITUTION；
   - 排球：SCORE、BLOCK、SERVE_ACE、ERROR；
   - 通用赛事：SCORE、FOUL。
7. 事件录入成功后自动刷新比赛详情、比分、统计、事件时间线和态势分析。
8. 支持查看实时统计，对接 GET /api/matches/{matchId}/statistics。
9. 支持生成实时态势分析，对接 GET /api/matches/{matchId}/analysis，该接口返回纯文本，必须用 response.text()。
10. 支持生成赛后复盘报告，对接 GET /api/matches/{matchId}/report，该接口返回纯文本，必须用 response.text()。
11. 所有 POST 接口返回 Result 对象时，要根据 success 字段显示成功或失败提示。
12. 不要把核心业务规则写死在前端，前端只做基础交互控制，最终校验以后端返回为准。
13. 页面要能用于答辩演示设计模式效果：
    - 状态模式：展示开始、暂停、恢复、结束；
    - 责任链模式：非法事件提示；
    - 策略模式：不同赛事事件类型和分析结果不同；
    - 观察者模式：事件录入后比分和统计自动刷新；
    - AI Agent：展示态势分析和赛后复盘文本。
14. 实现完成后，请输出新增文件列表、每个文件职责、核心代码说明和运行访问地址。
```

---

## 15. 总结

本项目前端应定位为“课程设计演示型管理界面”，核心目标是把后端的比赛管理、事件录入、统计更新、态势分析和赛后复盘能力完整串起来。

采用 `src/main/resources/static` 下的原生 HTML、CSS、JavaScript 实现有以下优点：

1. 不增加前端工程复杂度；
2. 与 Spring Boot 后端同源部署，不需要跨域配置；
3. 便于老师直接运行和查看；
4. 能清楚展示四种设计模式和 AI Agent 的业务落点；
5. 适合作为课程设计报告、运行截图和答辩演示材料。

最终前端应重点保证三件事：

1. **流程闭环**：创建比赛 → 配置队伍球员 → 开始比赛 → 录入事件 → 查看统计 → 态势分析 → 结束比赛 → 赛后复盘；
2. **模式可演示**：状态、责任链、策略、观察者都有对应页面操作；
3. **AI 可展示**：实时态势分析和赛后复盘结果能在页面中清楚呈现。
