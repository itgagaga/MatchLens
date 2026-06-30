# MatchLens 多页面前端设计方案与 AI 实现指令

> 项目：MatchLens 体育赛事数据智能统计与态势分析系统  
> 前端技术：HTML + CSS + JavaScript  
> 前端目录：`src/main/resources/static`  
> 设计目标：多页面、可跳转、色彩鲜艳、功能分区清晰、接口与后端功能对应。

---

## 1. 设计目标

当前项目不能把所有功能都堆在一个 `index.html` 页面中。新的前端要做成一个更像完整系统的多页面前端：

1. 首页只做系统概览、数据看板和功能入口。
2. 比赛管理、比赛详情、球队管理、球员管理、排行榜、AI 日志、报告中心分别做成独立页面。
3. 复杂功能必须通过点击跳转进入详情页，例如：
   - 点击比赛卡片，进入 `match-detail.html?matchId=xxx`
   - 点击球队的“查看球员”，进入 `players.html?teamId=xxx`
   - 点击比赛详情页的“查看 AI 日志”，进入 `ai-logs.html?matchId=xxx`
   - 点击比赛详情页的“查看历史报告”，进入 `reports.html?matchId=xxx`
4. 前端必须使用鲜艳配色，不能是一片白色。
5. 所有接口调用必须与后端接口功能对应。
6. 所有请求统一封装到 `js/api.js` 中，不要在每个页面中散落大量 `fetch`。
7. 每个页面单独使用一个 JS 文件，避免所有逻辑写进一个 `app.js`。

---

## 2. 前端目录结构

请将前端放在 Spring Boot 项目的：

```text
src/main/resources/static
```

推荐目录结构如下：

```text
src/main/resources/static/
├── index.html                    # 首页 / 数据看板 / 功能入口
├── matches.html                  # 比赛管理页
├── match-detail.html             # 比赛详情页
├── teams.html                    # 球队管理页
├── players.html                  # 球员管理页
├── rankings.html                 # 球员排行榜页
├── ai-logs.html                  # AI 调用日志页
├── reports.html                  # 报告中心页
├── css/
│   ├── theme.css                 # 全局主题、背景、导航栏、布局
│   ├── components.css            # 按钮、卡片、表格、表单、弹窗、Badge
│   └── pages.css                 # 各页面专属样式
└── js/
    ├── constants.js              # 枚举映射，例如比赛状态、赛事类型、事件类型
    ├── api.js                    # 后端接口统一封装
    ├── common.js                 # 通用工具方法
    └── pages/
        ├── dashboard.js          # 首页逻辑
        ├── matches.js            # 比赛管理页逻辑
        ├── match-detail.js       # 比赛详情页逻辑
        ├── teams.js              # 球队管理页逻辑
        ├── players.js            # 球员管理页逻辑
        ├── rankings.js           # 排行榜页逻辑
        ├── ai-logs.js            # AI 日志页逻辑
        └── reports.js            # 报告中心页逻辑
```

如果原来已有：

```text
index.html
css/style.css
js/api.js
js/app.js
js/render.js
js/constants.js
```

不要直接全部删除。建议处理方式：

1. 保留已有可用逻辑作为参考。
2. 将原来集中在 `app.js` 和 `render.js` 中的功能拆分到 `js/pages/` 下。
3. 将 `style.css` 中可用样式迁移到 `theme.css`、`components.css`、`pages.css`。
4. `index.html` 改造成首页，不再承载所有业务功能。

---

## 3. 页面划分与功能定位

| 页面 | 文件名 | 主要功能 | 是否复杂页面 |
|---|---|---|---|
| 首页 | `index.html` | 数据看板、功能入口、最近比赛、AI 概览 | 否 |
| 比赛管理 | `matches.html` | 创建比赛、查询比赛、筛选比赛、进入详情 | 中 |
| 比赛详情 | `match-detail.html` | 设置队伍、添加球员、状态控制、事件录入、统计、AI 分析、复盘 | 是 |
| 球队管理 | `teams.html` | 球队 CRUD、查看球员、查看历史比赛 | 中 |
| 球员管理 | `players.html` | 球员 CRUD、按球队筛选、查看事件和统计 | 中 |
| 排行榜 | `rankings.html` | 得分榜、助攻榜、篮板榜、全局排行榜 | 中 |
| AI 日志 | `ai-logs.html` | 查询 AI 调用日志、查看 Prompt 和 Response | 中 |
| 报告中心 | `reports.html` | 查看历史报告、生成报告、删除报告、Markdown 预览 | 中 |

---

## 4. 页面跳转关系

```text
index.html
├── 点击“比赛管理” → matches.html
├── 点击“球队管理” → teams.html
├── 点击“球员管理” → players.html
├── 点击“排行榜” → rankings.html
├── 点击“AI 日志” → ai-logs.html
└── 点击“报告中心” → reports.html

matches.html
└── 点击某场比赛“查看详情” → match-detail.html?matchId=xxx

match-detail.html
├── 点击“查看历史报告” → reports.html?matchId=xxx
└── 点击“查看 AI 日志” → ai-logs.html?matchId=xxx

teams.html
└── 点击某球队“查看球员” → players.html?teamId=xxx

players.html
└── 点击“查看排行榜” → rankings.html
```

---

## 5. 统一导航栏设计

每个页面顶部都要有统一导航栏：

```html
<header class="topbar">
  <a class="brand" href="./index.html">🏆 MatchLens</a>
  <nav class="nav">
    <a class="nav-link" href="./index.html">首页</a>
    <a class="nav-link" href="./matches.html">比赛管理</a>
    <a class="nav-link" href="./teams.html">球队管理</a>
    <a class="nav-link" href="./players.html">球员管理</a>
    <a class="nav-link" href="./rankings.html">排行榜</a>
    <a class="nav-link" href="./ai-logs.html">AI 日志</a>
    <a class="nav-link" href="./reports.html">报告中心</a>
  </nav>
</header>
```

导航栏要求：

1. 背景使用深色或渐变色。
2. 当前页面导航项高亮。
3. 鼠标悬浮时有颜色变化。
4. 所有页面导航栏保持一致。

---

## 6. 鲜艳视觉风格设计

### 6.1 整体风格

前端不能做成一片白色。推荐采用：

```text
体育数据大屏 + 彩色管理后台
```

视觉关键词：

```text
深色渐变背景
彩色功能卡片
渐变按钮
醒目比分板
彩色状态标签
排行榜奖牌效果
AI 日志深色代码块
```

### 6.2 推荐配色

| 用途 | 颜色 |
|---|---|
| 页面背景 | 深蓝、深紫、黑蓝渐变 |
| 主色 | 蓝紫色 `#4f46e5` |
| 强调色 | 橙色 `#f97316` |
| 科技色 | 青色 `#06b6d4` |
| 成功色 | 绿色 `#22c55e` |
| 警告色 | 黄色 `#facc15` |
| 危险色 | 红色 `#ef4444` |
| 卡片背景 | 半透明白色或半透明深色 |

### 6.3 `css/theme.css` 基础样式

```css
:root {
  --bg-main: #0f172a;
  --bg-card: rgba(255, 255, 255, 0.92);
  --primary: #4f46e5;
  --primary-dark: #3730a3;
  --secondary: #f97316;
  --accent: #06b6d4;
  --success: #22c55e;
  --warning: #facc15;
  --danger: #ef4444;
  --text-main: #0f172a;
  --text-light: #f8fafc;
  --text-muted: #64748b;
  --border: rgba(148, 163, 184, 0.35);
  --shadow: 0 18px 48px rgba(15, 23, 42, 0.22);
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  min-height: 100vh;
  font-family: "Microsoft YaHei", "PingFang SC", Arial, sans-serif;
  color: var(--text-main);
  background:
    radial-gradient(circle at 8% 8%, rgba(79, 70, 229, 0.45), transparent 28%),
    radial-gradient(circle at 92% 18%, rgba(249, 115, 22, 0.35), transparent 25%),
    radial-gradient(circle at 60% 90%, rgba(6, 182, 212, 0.35), transparent 26%),
    linear-gradient(135deg, #020617 0%, #0f172a 50%, #1e1b4b 100%);
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  height: 68px;
  padding: 0 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(2, 6, 23, 0.82);
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(14px);
}

.brand {
  color: var(--text-light);
  font-size: 24px;
  font-weight: 900;
  text-decoration: none;
  letter-spacing: 0.5px;
}

.nav {
  display: flex;
  gap: 10px;
}

.nav-link {
  color: #cbd5e1;
  text-decoration: none;
  padding: 10px 14px;
  border-radius: 999px;
  transition: all 0.2s ease;
}

.nav-link:hover,
.nav-link.active {
  color: white;
  background: linear-gradient(135deg, var(--primary), var(--accent));
}

.page {
  width: min(1280px, calc(100% - 48px));
  margin: 0 auto;
  padding: 32px 0 56px;
}

.page-hero {
  color: white;
  padding: 34px;
  margin-bottom: 24px;
  border-radius: 28px;
  background:
    linear-gradient(135deg, rgba(79, 70, 229, 0.94), rgba(6, 182, 212, 0.82)),
    linear-gradient(45deg, rgba(249, 115, 22, 0.2), transparent);
  box-shadow: 0 24px 70px rgba(79, 70, 229, 0.35);
}

.page-hero h1 {
  margin: 0 0 10px;
  font-size: 36px;
}

.page-hero p {
  margin: 0;
  color: #e0f2fe;
}
```

### 6.4 `css/components.css` 组件样式

```css
.card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 22px;
  padding: 22px;
  box-shadow: var(--shadow);
  backdrop-filter: blur(14px);
}

.grid {
  display: grid;
  gap: 20px;
}

.grid-4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.grid-3 {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.btn {
  border: none;
  border-radius: 999px;
  padding: 11px 18px;
  cursor: pointer;
  color: white;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary), var(--accent));
  box-shadow: 0 10px 24px rgba(79, 70, 229, 0.25);
}

.btn:hover {
  transform: translateY(-1px);
  filter: brightness(1.05);
}

.btn-orange {
  background: linear-gradient(135deg, var(--secondary), var(--warning));
}

.btn-green {
  background: linear-gradient(135deg, var(--success), #14b8a6);
}

.btn-red {
  background: linear-gradient(135deg, var(--danger), var(--secondary));
}

.badge {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 12px;
  font-weight: 800;
  color: white;
}

.badge-blue {
  background: linear-gradient(135deg, var(--primary), var(--accent));
}

.badge-green {
  background: linear-gradient(135deg, var(--success), #14b8a6);
}

.badge-orange {
  background: linear-gradient(135deg, var(--secondary), var(--warning));
}

.badge-red {
  background: linear-gradient(135deg, var(--danger), #fb7185);
}

input,
select,
textarea {
  width: 100%;
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 12px 14px;
  outline: none;
  font-size: 14px;
}

input:focus,
select:focus,
textarea:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.15);
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th {
  text-align: left;
  color: #475569;
  padding: 12px;
  border-bottom: 1px solid #e2e8f0;
}

.table td {
  padding: 12px;
  border-bottom: 1px solid #e2e8f0;
}

.toast {
  position: fixed;
  right: 24px;
  bottom: 24px;
  padding: 14px 18px;
  border-radius: 14px;
  color: white;
  background: #0f172a;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.2s ease;
  z-index: 100;
}

.toast.show {
  opacity: 1;
  transform: translateY(0);
}

.empty {
  padding: 32px;
  text-align: center;
  color: var(--text-muted);
}
```

---

## 7. 统一 HTML 页面骨架

所有页面采用统一结构：

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>MatchLens - 页面标题</title>
  <link rel="stylesheet" href="./css/theme.css" />
  <link rel="stylesheet" href="./css/components.css" />
  <link rel="stylesheet" href="./css/pages.css" />
</head>
<body>
  <header class="topbar">
    <a class="brand" href="./index.html">🏆 MatchLens</a>
    <nav class="nav">
      <a class="nav-link" href="./index.html">首页</a>
      <a class="nav-link" href="./matches.html">比赛管理</a>
      <a class="nav-link" href="./teams.html">球队管理</a>
      <a class="nav-link" href="./players.html">球员管理</a>
      <a class="nav-link" href="./rankings.html">排行榜</a>
      <a class="nav-link" href="./ai-logs.html">AI 日志</a>
      <a class="nav-link" href="./reports.html">报告中心</a>
    </nav>
  </header>

  <main class="page">
    <section class="page-hero">
      <h1>页面标题</h1>
      <p>页面说明文字</p>
    </section>

    <section class="card">
      页面主体内容
    </section>
  </main>

  <div id="toast" class="toast"></div>

  <script src="./js/constants.js"></script>
  <script src="./js/api.js"></script>
  <script src="./js/common.js"></script>
  <script src="./js/pages/当前页面.js"></script>
</body>
</html>
```

---

## 8. 首页 `index.html`

### 8.1 页面定位

首页只负责系统概览，不做复杂业务录入。

### 8.2 首页模块

| 模块 | 内容 | 接口 |
|---|---|---|
| 数据看板 | 比赛总数、球队总数、球员总数、事件总数、AI 成功率 | `GET /api/dashboard/summary` |
| 功能入口 | 比赛管理、球队管理、球员管理、排行榜、AI 日志、报告中心 | 无接口 |
| 最近比赛 | 最近几场比赛 | `GET /api/matches` |
| AI 概览 | AI 调用数、成功数、失败数 | `GET /api/dashboard/summary` |

### 8.3 首页示例结构

```html
<section class="page-hero">
  <h1>MatchLens 体育赛事智能分析系统</h1>
  <p>面向多赛事的数据采集、统计分析、AI 态势研判与赛后复盘平台</p>
</section>

<section class="grid grid-4" id="summaryCards"></section>

<section class="grid grid-3">
  <a class="feature-card" href="./matches.html">🏀 比赛管理</a>
  <a class="feature-card orange" href="./teams.html">🔥 球队管理</a>
  <a class="feature-card green" href="./players.html">⭐ 球员管理</a>
  <a class="feature-card red" href="./ai-logs.html">🤖 AI 日志</a>
</section>

<section class="card">
  <h2>最近比赛</h2>
  <div id="recentMatches"></div>
</section>
```

---

## 9. 比赛管理页 `matches.html`

### 9.1 页面功能

1. 创建比赛。
2. 查询比赛列表。
3. 按赛事类型筛选。
4. 按比赛状态筛选。
5. 点击比赛进入详情页。

### 9.2 对应接口

| 功能 | 接口 |
|---|---|
| 查询比赛列表 | `GET /api/matches` |
| 创建比赛 | `POST /api/matches` |
| 查询比赛详情 | `GET /api/matches/{id}` |
| 条件查询比赛 | `GET /api/matches?keyword=&sportType=&status=` |

### 9.3 页面结构

```text
页面 Hero
↓
创建比赛表单
↓
筛选区域
↓
比赛卡片列表
```

### 9.4 比赛卡片

每张比赛卡片展示：

```text
比赛名称
赛事类型
比赛状态
主队 vs 客队
当前比分
比赛时间
场馆
查看详情按钮
```

点击详情按钮：

```js
location.href = `match-detail.html?matchId=${match.matchId}`;
```

---

## 10. 比赛详情页 `match-detail.html`

### 10.1 页面定位

比赛详情页是核心演示页，负责完整比赛流程。

URL 示例：

```text
match-detail.html?matchId=m001
```

页面通过 `matchId` 获取比赛详情。

### 10.2 页面功能

1. 展示比赛基本信息。
2. 展示醒目的比分板。
3. 设置主队和客队。
4. 添加球员。
5. 控制比赛状态：开始、暂停、恢复、结束。
6. 录入比赛事件。
7. 展示事件时间线。
8. 展示实时统计。
9. 调用 AI 生成实时态势分析。
10. 调用 AI 生成赛后复盘。
11. 跳转查看 AI 日志。
12. 跳转查看历史报告。

### 10.3 对应接口

| 功能 | 接口 |
|---|---|
| 查询比赛详情 | `GET /api/matches/{id}` |
| 设置主客队 | `POST /api/matches/{id}/teams` |
| 添加球员 | `POST /api/matches/{id}/players` |
| 开始比赛 | `POST /api/matches/{id}/start` |
| 暂停比赛 | `POST /api/matches/{id}/pause` |
| 恢复比赛 | `POST /api/matches/{id}/resume` |
| 结束比赛 | `POST /api/matches/{id}/finish` |
| 录入事件 | `POST /api/matches/{id}/events` |
| 获取统计 | `GET /api/matches/{id}/statistics` |
| 生成态势分析 | `GET /api/matches/{id}/analysis` |
| 生成赛后复盘 | `GET /api/matches/{id}/report` |
| 查询事件时间线 | `GET /api/matches/{id}/events` |
| 修改事件 | `PUT /api/events/{eventId}` |
| 删除事件 | `DELETE /api/events/{eventId}` |

### 10.4 比分板样式

```html
<section class="score-hero">
  <div class="team-name" id="homeTeamName">主队</div>
  <div class="score-number">
    <span id="homeScore">0</span>
    <span>:</span>
    <span id="awayScore">0</span>
  </div>
  <div class="team-name" id="awayTeamName">客队</div>
</section>
```

```css
.score-hero {
  color: white;
  padding: 32px;
  border-radius: 28px;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 24px;
  background:
    linear-gradient(135deg, rgba(79, 70, 229, 0.96), rgba(6, 182, 212, 0.86)),
    linear-gradient(45deg, rgba(249, 115, 22, 0.32), transparent);
  box-shadow: 0 26px 70px rgba(79, 70, 229, 0.38);
}

.team-name {
  font-size: 28px;
  font-weight: 900;
  text-align: center;
}

.score-number {
  font-size: 62px;
  font-weight: 1000;
  letter-spacing: 3px;
}
```

---

## 11. 球队管理页 `teams.html`

### 11.1 页面功能

1. 查询球队列表。
2. 新增球队。
3. 修改球队。
4. 删除球队。
5. 查看某球队球员。
6. 查看某球队历史比赛。

### 11.2 对应接口

| 功能 | 接口 |
|---|---|
| 查询球队列表 | `GET /api/teams` |
| 查询球队详情 | `GET /api/teams/{teamId}` |
| 新增球队 | `POST /api/teams` |
| 修改球队 | `PUT /api/teams/{teamId}` |
| 删除球队 | `DELETE /api/teams/{teamId}` |
| 查询球队球员 | `GET /api/teams/{teamId}/players` |
| 查询球队历史比赛 | `GET /api/teams/{teamId}/matches` |

### 11.3 页面布局

```text
左侧：新增/编辑球队表单
右侧：球队卡片列表
弹窗：球队球员、历史比赛
```

### 11.4 表单字段

```text
球队名称 teamName
城市 city
教练 coachName
队徽 logoUrl
```

---

## 12. 球员管理页 `players.html`

### 12.1 页面功能

1. 查询球员列表。
2. 按球队筛选球员。
3. 新增球员。
4. 修改球员。
5. 删除球员。
6. 查看球员事件记录。
7. 查看球员统计数据。

### 12.2 对应接口

| 功能 | 接口 |
|---|---|
| 查询球员列表 | `GET /api/players` |
| 按球队查询球员 | `GET /api/players?teamId=xxx` |
| 查询球员详情 | `GET /api/players/{playerId}` |
| 新增球员 | `POST /api/players` |
| 修改球员 | `PUT /api/players/{playerId}` |
| 删除球员 | `DELETE /api/players/{playerId}` |
| 查询球员事件 | `GET /api/players/{playerId}/events` |
| 查询球员统计 | `GET /api/players/{playerId}/statistics` |

### 12.3 表单字段

```text
球员姓名 playerName
所属球队 teamId
球衣号码 number
位置 position
年龄 age
身高 height
体重 weight
```

---

## 13. 排行榜页 `rankings.html`

### 13.1 页面功能

1. 查询某场比赛排行榜。
2. 查询全局排行榜。
3. 按统计维度切换。
4. 前三名使用奖牌视觉效果。

### 13.2 对应接口

| 功能 | 接口 |
|---|---|
| 查询比赛列表 | `GET /api/matches` |
| 某场比赛排行榜 | `GET /api/rankings/players?matchId=xxx&statKey=SCORE&limit=10` |
| 全局排行榜 | `GET /api/rankings/players/overall?statKey=SCORE&limit=10` |

### 13.3 统计维度

```text
SCORE 得分
ASSIST 助攻
REBOUND 篮板
FOUL 犯规
STEAL 抢断
TURNOVER 失误
```

---

## 14. AI 调用日志页 `ai-logs.html`

### 14.1 页面功能

1. 查询 AI 调用日志。
2. 按比赛筛选。
3. 按 Agent 类型筛选。
4. 按成功/失败筛选。
5. 查看 Prompt。
6. 查看 Response。
7. 查看错误信息。
8. 删除日志。

### 14.2 对应接口

| 功能 | 接口 |
|---|---|
| 查询 AI 日志列表 | `GET /api/ai-logs?matchId=&agentType=&success=&keyword=` |
| 查询日志详情 | `GET /api/ai-logs/{id}` |
| 查询某场比赛日志 | `GET /api/matches/{matchId}/ai-logs` |
| 删除日志 | `DELETE /api/ai-logs/{id}` |

### 14.3 日志详情显示

Prompt 和 Response 使用深色代码块：

```css
.log-code {
  background: #020617;
  color: #e2e8f0;
  border-radius: 16px;
  padding: 18px;
  max-height: 380px;
  overflow: auto;
  white-space: pre-wrap;
}
```

---

## 15. 报告中心页 `reports.html`

### 15.1 页面功能

1. 查询某场比赛的历史报告。
2. 查看报告详情。
3. 生成并保存实时态势报告。
4. 生成并保存赛后复盘报告。
5. 删除报告。
6. Markdown 预览报告内容。

### 15.2 对应接口

| 功能 | 接口 |
|---|---|
| 查询某场比赛报告 | `GET /api/matches/{matchId}/reports` |
| 查询报告详情 | `GET /api/reports/{reportId}` |
| 手动保存报告 | `POST /api/matches/{matchId}/reports` |
| 生成并保存态势报告 | `POST /api/matches/{matchId}/reports/generate?reportType=SITUATION` |
| 生成并保存复盘报告 | `POST /api/matches/{matchId}/reports/generate?reportType=REVIEW` |
| 删除报告 | `DELETE /api/reports/{reportId}` |

### 15.3 页面布局

```text
顶部：比赛筛选、报告类型筛选
左侧：报告列表
右侧：报告详情和 Markdown 预览
底部：生成态势报告、生成复盘报告、删除报告
```

---

## 16. `js/constants.js` 设计

```js
const SPORT_TYPE_TEXT = {
  BASKETBALL: '篮球',
  FOOTBALL: '足球',
  VOLLEYBALL: '排球',
  GENERAL: '通用赛事'
};

const MATCH_STATUS_TEXT = {
  NOT_STARTED: '未开始',
  RUNNING: '进行中',
  PAUSED: '暂停中',
  FINISHED: '已结束'
};

const EVENT_TYPE_TEXT = {
  SCORE: '得分',
  ASSIST: '助攻',
  REBOUND: '篮板',
  FOUL: '犯规',
  STEAL: '抢断',
  TURNOVER: '失误',
  RED_CARD: '红牌',
  YELLOW_CARD: '黄牌',
  SPIKE: '扣球',
  BLOCK: '拦网'
};

const STAT_KEY_TEXT = {
  SCORE: '得分',
  ASSIST: '助攻',
  REBOUND: '篮板',
  FOUL: '犯规',
  STEAL: '抢断',
  TURNOVER: '失误'
};
```

---

## 17. `js/common.js` 设计

```js
function getQueryParam(name) {
  return new URLSearchParams(location.search).get(name);
}

function toQuery(params = {}) {
  const search = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.append(key, value);
    }
  });

  const query = search.toString();
  return query ? `?${query}` : '';
}

function showToast(message, type = 'success') {
  const toast = document.querySelector('#toast');
  if (!toast) {
    alert(message);
    return;
  }

  toast.textContent = message;
  toast.className = `toast show ${type}`;

  setTimeout(() => {
    toast.className = 'toast';
  }, 2200);
}

function setActiveNav() {
  const current = location.pathname.split('/').pop() || 'index.html';
  document.querySelectorAll('.nav-link').forEach(link => {
    const href = link.getAttribute('href');
    if (href === `./${current}` || href === current) {
      link.classList.add('active');
    }
  });
}

function safeText(value, fallback = '-') {
  return value === undefined || value === null || value === '' ? fallback : value;
}

function renderEmpty(text = '暂无数据') {
  return `<div class="empty">${text}</div>`;
}

document.addEventListener('DOMContentLoaded', setActiveNav);
```

---

## 18. `js/api.js` 统一接口封装

所有接口都放在 `api.js` 中。

### 18.1 通用请求方法

```js
const api = {
  async request(url, options = {}, responseType = 'json') {
    const config = {
      headers: {
        'Content-Type': 'application/json'
      },
      ...options
    };

    try {
      const response = await fetch(url, config);

      if (!response.ok) {
        throw new Error(`请求失败，状态码：${response.status}`);
      }

      if (responseType === 'text') {
        return await response.text();
      }

      const result = await response.json();

      // 兼容两种后端返回：
      // 1. 直接返回对象或数组
      // 2. 返回 Result<T>，如 { success, message, data }
      if (result && typeof result === 'object' && 'success' in result && 'data' in result) {
        if (!result.success) {
          throw new Error(result.message || '操作失败');
        }
        return result.data;
      }

      return result;
    } catch (error) {
      console.error('API 请求异常：', url, error);
      throw error;
    }
  },

  getDashboardSummary() {
    return this.request('/api/dashboard/summary');
  },

  getMatches(params = {}) {
    return this.request(`/api/matches${toQuery(params)}`);
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

  addPlayerToMatch(matchId, data) {
    return this.request(`/api/matches/${matchId}/players`, {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  changeMatchStatus(matchId, action) {
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

  getMatchEvents(matchId) {
    return this.request(`/api/matches/${matchId}/events`);
  },

  updateEvent(eventId, data) {
    return this.request(`/api/events/${eventId}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  deleteEvent(eventId) {
    return this.request(`/api/events/${eventId}`, {
      method: 'DELETE'
    });
  },

  getStatistics(matchId) {
    return this.request(`/api/matches/${matchId}/statistics`);
  },

  getAnalysis(matchId) {
    return this.request(`/api/matches/${matchId}/analysis`, {}, 'text');
  },

  getReviewReport(matchId) {
    return this.request(`/api/matches/${matchId}/report`, {}, 'text');
  },

  getTeams() {
    return this.request('/api/teams');
  },

  getTeam(teamId) {
    return this.request(`/api/teams/${teamId}`);
  },

  createTeam(data) {
    return this.request('/api/teams', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  updateTeam(teamId, data) {
    return this.request(`/api/teams/${teamId}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  deleteTeam(teamId) {
    return this.request(`/api/teams/${teamId}`, {
      method: 'DELETE'
    });
  },

  getTeamPlayers(teamId) {
    return this.request(`/api/teams/${teamId}/players`);
  },

  getTeamMatches(teamId) {
    return this.request(`/api/teams/${teamId}/matches`);
  },

  getPlayers(params = {}) {
    return this.request(`/api/players${toQuery(params)}`);
  },

  getPlayer(playerId) {
    return this.request(`/api/players/${playerId}`);
  },

  createPlayer(data) {
    return this.request('/api/players', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  updatePlayer(playerId, data) {
    return this.request(`/api/players/${playerId}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  deletePlayer(playerId) {
    return this.request(`/api/players/${playerId}`, {
      method: 'DELETE'
    });
  },

  getPlayerEvents(playerId) {
    return this.request(`/api/players/${playerId}/events`);
  },

  getPlayerStatistics(playerId) {
    return this.request(`/api/players/${playerId}/statistics`);
  },

  getPlayerRankings(params = {}) {
    return this.request(`/api/rankings/players${toQuery(params)}`);
  },

  getOverallPlayerRankings(params = {}) {
    return this.request(`/api/rankings/players/overall${toQuery(params)}`);
  },

  getAiLogs(params = {}) {
    return this.request(`/api/ai-logs${toQuery(params)}`);
  },

  getAiLog(id) {
    return this.request(`/api/ai-logs/${id}`);
  },

  deleteAiLog(id) {
    return this.request(`/api/ai-logs/${id}`, {
      method: 'DELETE'
    });
  },

  getMatchAiLogs(matchId) {
    return this.request(`/api/matches/${matchId}/ai-logs`);
  },

  getMatchReports(matchId) {
    return this.request(`/api/matches/${matchId}/reports`);
  },

  getReportDetail(reportId) {
    return this.request(`/api/reports/${reportId}`);
  },

  generateAndSaveReport(matchId, reportType) {
    return this.request(`/api/matches/${matchId}/reports/generate?reportType=${reportType}`, {
      method: 'POST'
    });
  },

  deleteReport(reportId) {
    return this.request(`/api/reports/${reportId}`, {
      method: 'DELETE'
    });
  }
};
```

---

## 19. 后端接口对应总表

### 19.1 已有核心接口

| 模块 | 方法 | 路径 | 前端页面 |
|---|---|---|---|
| 比赛 | GET | `/api/matches` | `index.html`、`matches.html`、`rankings.html` |
| 比赛 | GET | `/api/matches/{id}` | `match-detail.html` |
| 比赛 | POST | `/api/matches` | `matches.html` |
| 队伍设置 | POST | `/api/matches/{id}/teams` | `match-detail.html` |
| 球员添加 | POST | `/api/matches/{id}/players` | `match-detail.html` |
| 状态控制 | POST | `/api/matches/{id}/start` | `match-detail.html` |
| 状态控制 | POST | `/api/matches/{id}/pause` | `match-detail.html` |
| 状态控制 | POST | `/api/matches/{id}/resume` | `match-detail.html` |
| 状态控制 | POST | `/api/matches/{id}/finish` | `match-detail.html` |
| 事件录入 | POST | `/api/matches/{id}/events` | `match-detail.html` |
| 统计 | GET | `/api/matches/{id}/statistics` | `match-detail.html` |
| AI 态势 | GET | `/api/matches/{id}/analysis` | `match-detail.html` |
| AI 复盘 | GET | `/api/matches/{id}/report` | `match-detail.html` |

### 19.2 建议新增接口

| 模块 | 方法 | 路径 | 前端页面 |
|---|---|---|---|
| 首页看板 | GET | `/api/dashboard/summary` | `index.html` |
| 球队 | GET | `/api/teams` | `teams.html` |
| 球队 | GET | `/api/teams/{teamId}` | `teams.html` |
| 球队 | POST | `/api/teams` | `teams.html` |
| 球队 | PUT | `/api/teams/{teamId}` | `teams.html` |
| 球队 | DELETE | `/api/teams/{teamId}` | `teams.html` |
| 球队 | GET | `/api/teams/{teamId}/players` | `teams.html` |
| 球队 | GET | `/api/teams/{teamId}/matches` | `teams.html` |
| 球员 | GET | `/api/players` | `players.html` |
| 球员 | GET | `/api/players/{playerId}` | `players.html` |
| 球员 | POST | `/api/players` | `players.html` |
| 球员 | PUT | `/api/players/{playerId}` | `players.html` |
| 球员 | DELETE | `/api/players/{playerId}` | `players.html` |
| 球员 | GET | `/api/players/{playerId}/events` | `players.html` |
| 球员 | GET | `/api/players/{playerId}/statistics` | `players.html` |
| 事件 | GET | `/api/matches/{matchId}/events` | `match-detail.html` |
| 事件 | GET | `/api/events/{eventId}` | `match-detail.html` |
| 事件 | PUT | `/api/events/{eventId}` | `match-detail.html` |
| 事件 | DELETE | `/api/events/{eventId}` | `match-detail.html` |
| 排行榜 | GET | `/api/rankings/players` | `rankings.html` |
| 排行榜 | GET | `/api/rankings/players/overall` | `rankings.html` |
| AI 日志 | GET | `/api/ai-logs` | `ai-logs.html` |
| AI 日志 | GET | `/api/ai-logs/{id}` | `ai-logs.html` |
| AI 日志 | GET | `/api/matches/{matchId}/ai-logs` | `ai-logs.html` |
| AI 日志 | DELETE | `/api/ai-logs/{id}` | `ai-logs.html` |
| 报告 | GET | `/api/matches/{matchId}/reports` | `reports.html` |
| 报告 | GET | `/api/reports/{reportId}` | `reports.html` |
| 报告 | POST | `/api/matches/{matchId}/reports/generate` | `reports.html` |
| 报告 | DELETE | `/api/reports/{reportId}` | `reports.html` |

---

## 20. AI 编程工具总指令

下面这段可以直接复制给 AI 编程工具，让它按要求生成前端。

```text
你现在接手的是一个 Spring Boot 项目：MatchLens 体育赛事数据智能统计与态势分析系统。

前端技术限定为 HTML + CSS + 原生 JavaScript。
前端目录固定为：src/main/resources/static。
不要使用 Vue、React、Angular，不要引入复杂构建工具。

当前目标：把前端设计成多页面系统，而不是把所有功能堆在一个 index.html 页面中。页面必须鲜艳，有深色渐变背景、彩色卡片、渐变按钮、状态标签、醒目的比分板，不能是一片白。

一、目录要求

请在 src/main/resources/static 下创建或调整以下文件：

1. index.html：首页 / 数据看板 / 功能入口
2. matches.html：比赛管理页
3. match-detail.html：比赛详情页
4. teams.html：球队管理页
5. players.html：球员管理页
6. rankings.html：球员排行榜页
7. ai-logs.html：AI 调用日志页
8. reports.html：报告中心页

样式文件：
1. css/theme.css
2. css/components.css
3. css/pages.css

脚本文件：
1. js/constants.js
2. js/api.js
3. js/common.js
4. js/pages/dashboard.js
5. js/pages/matches.js
6. js/pages/match-detail.js
7. js/pages/teams.js
8. js/pages/players.js
9. js/pages/rankings.js
10. js/pages/ai-logs.js
11. js/pages/reports.js

二、页面要求

1. 所有页面必须有统一顶部导航栏：
   - 首页
   - 比赛管理
   - 球队管理
   - 球员管理
   - 排行榜
   - AI 日志
   - 报告中心

2. 首页 index.html 只展示：
   - 系统标题
   - 数据看板
   - 彩色功能入口
   - 最近比赛
   - AI 调用概览
   不允许把事件录入、状态控制、AI 复盘等复杂功能放在首页。

3. 比赛管理 matches.html：
   - 查询比赛列表
   - 创建比赛
   - 按赛事类型筛选
   - 按比赛状态筛选
   - 比赛用彩色卡片展示
   - 点击“查看详情”跳转到 match-detail.html?matchId=xxx

4. 比赛详情 match-detail.html：
   - 从 URL 中读取 matchId
   - 查询比赛详情
   - 展示醒目的比分板
   - 设置主客队
   - 添加球员
   - 开始、暂停、恢复、结束比赛
   - 录入比赛事件
   - 展示事件时间线
   - 展示实时统计
   - 调用 AI 生成实时态势分析
   - 调用 AI 生成赛后复盘
   - 提供按钮跳转到 reports.html?matchId=xxx
   - 提供按钮跳转到 ai-logs.html?matchId=xxx

5. 球队管理 teams.html：
   - 查询球队
   - 新增球队
   - 编辑球队
   - 删除球队
   - 查看球队球员
   - 查看球队历史比赛

6. 球员管理 players.html：
   - 查询球员
   - 支持 URL 参数 teamId，例如 players.html?teamId=xxx
   - 按球队筛选球员
   - 新增球员
   - 编辑球员
   - 删除球员
   - 查看球员事件
   - 查看球员统计

7. 排行榜 rankings.html：
   - 支持选择比赛
   - 支持选择统计维度：SCORE、ASSIST、REBOUND、FOUL、STEAL、TURNOVER
   - 支持某场比赛排行榜
   - 支持全局排行榜
   - 前三名使用奖牌视觉效果

8. AI 日志 ai-logs.html：
   - 查询 AI 调用日志
   - 支持 URL 参数 matchId，例如 ai-logs.html?matchId=xxx
   - 按成功/失败筛选
   - 按 Agent 类型筛选
   - 查看 Prompt
   - 查看 Response
   - 查看错误信息
   - Prompt 和 Response 使用深色代码块展示

9. 报告中心 reports.html：
   - 支持 URL 参数 matchId，例如 reports.html?matchId=xxx
   - 查询某场比赛历史报告
   - 查看报告详情
   - 生成并保存实时态势报告
   - 生成并保存赛后复盘报告
   - 删除报告
   - Markdown 形式预览报告内容

三、接口要求

所有接口调用统一写在 js/api.js，不要在各页面 JS 中散落 fetch。

必须封装已有接口：

- GET /api/matches
- GET /api/matches/{id}
- POST /api/matches
- POST /api/matches/{id}/teams
- POST /api/matches/{id}/players
- POST /api/matches/{id}/start
- POST /api/matches/{id}/pause
- POST /api/matches/{id}/resume
- POST /api/matches/{id}/finish
- POST /api/matches/{id}/events
- GET /api/matches/{id}/statistics
- GET /api/matches/{id}/analysis
- GET /api/matches/{id}/report

同时预留新增接口：

- GET /api/dashboard/summary
- GET /api/teams
- GET /api/teams/{teamId}
- POST /api/teams
- PUT /api/teams/{teamId}
- DELETE /api/teams/{teamId}
- GET /api/teams/{teamId}/players
- GET /api/teams/{teamId}/matches
- GET /api/players
- GET /api/players/{playerId}
- POST /api/players
- PUT /api/players/{playerId}
- DELETE /api/players/{playerId}
- GET /api/players/{playerId}/events
- GET /api/players/{playerId}/statistics
- GET /api/matches/{matchId}/events
- GET /api/events/{eventId}
- PUT /api/events/{eventId}
- DELETE /api/events/{eventId}
- GET /api/rankings/players
- GET /api/rankings/players/overall
- GET /api/ai-logs
- GET /api/ai-logs/{id}
- GET /api/matches/{matchId}/ai-logs
- DELETE /api/ai-logs/{id}
- GET /api/matches/{matchId}/reports
- GET /api/reports/{reportId}
- POST /api/matches/{matchId}/reports/generate
- DELETE /api/reports/{reportId}

四、兼容要求

1. 后端可能直接返回对象或数组，也可能返回 Result<T>，api.js 必须兼容。
2. 如果某个新增接口后端暂时还没实现，页面不能白屏，要提示“该功能需要后端接口支持”。
3. 所有列表为空时要显示空状态，例如“暂无比赛”“暂无球员”“暂无 AI 调用记录”。
4. 所有增删改操作完成后要刷新列表。
5. 所有表单提交前要进行基本校验。
6. 不要破坏已有后端接口。
7. 不要修改后端 Controller 路径，除非我明确要求。
8. 不要把页面写成纯白背景。

五、最终输出

请输出：
1. 新增文件列表
2. 修改文件列表
3. 每个页面对应的接口
4. 页面跳转关系
5. 测试步骤
6. 如果有后端接口缺失，请列出需要补充的接口
```

---

## 21. 分阶段实现指令

如果一次性让 AI 生成太多，可以分三次执行。

### 阶段一：生成多页面骨架和鲜艳主题

```text
请先只生成 MatchLens 的多页面前端骨架和鲜艳主题样式。

要求：
1. 前端目录是 src/main/resources/static。
2. 使用 HTML + CSS + 原生 JavaScript。
3. 创建 index.html、matches.html、match-detail.html、teams.html、players.html、rankings.html、ai-logs.html、reports.html。
4. 创建 css/theme.css、css/components.css、css/pages.css。
5. 所有页面都有统一顶部导航栏。
6. 页面背景使用深蓝紫渐变，不允许纯白。
7. 首页有彩色功能入口卡片。
8. 比赛管理页中放几个静态比赛卡片，占位按钮可以跳转到 match-detail.html?matchId=m001。
9. 暂时不要接入真实接口。
10. 不修改后端代码。
```

### 阶段二：接入现有比赛核心接口

```text
请在已经生成的多页面前端基础上，接入现有比赛核心接口。

要求：
1. 在 js/api.js 中封装：
   - GET /api/matches
   - GET /api/matches/{id}
   - POST /api/matches
   - POST /api/matches/{id}/teams
   - POST /api/matches/{id}/players
   - POST /api/matches/{id}/start
   - POST /api/matches/{id}/pause
   - POST /api/matches/{id}/resume
   - POST /api/matches/{id}/finish
   - POST /api/matches/{id}/events
   - GET /api/matches/{id}/statistics
   - GET /api/matches/{id}/analysis
   - GET /api/matches/{id}/report

2. matches.html 实现：
   - 查询比赛列表
   - 创建比赛
   - 点击进入 match-detail.html?matchId=xxx

3. match-detail.html 实现：
   - 根据 URL 参数 matchId 查询比赛详情
   - 设置主客队
   - 添加球员
   - 状态控制
   - 事件录入
   - 统计展示
   - AI 态势分析
   - 赛后复盘

4. 保持多页面结构，不要把功能移回 index.html。
5. 保持鲜艳主题。
```

### 阶段三：接入新增 CRUD 和增强页面

```text
请继续完善 MatchLens 多页面前端，接入新增 CRUD 和增强接口。

要求：
1. teams.html 接入 /api/teams 系列接口。
2. players.html 接入 /api/players 系列接口。
3. rankings.html 接入 /api/rankings 系列接口。
4. ai-logs.html 接入 /api/ai-logs 系列接口。
5. reports.html 接入 /api/reports 和 /api/matches/{matchId}/reports 系列接口。
6. match-detail.html 增加事件时间线，调用 GET /api/matches/{matchId}/events。
7. 如果新增接口后端暂时不存在，要提示用户“该功能需要后端接口支持”，不要页面白屏。
8. 每个页面逻辑放入 js/pages/ 对应文件中。
9. 继续保持鲜艳主题和统一导航栏。
```

---

## 22. 建议保留的答辩截图

前端完成后，建议截图以下页面：

1. 首页数据看板
2. 首页彩色功能入口
3. 比赛管理页
4. 创建比赛表单
5. 比赛详情页比分板
6. 比赛状态控制按钮
7. 事件录入区域
8. 事件时间线
9. 实时统计区域
10. AI 态势分析结果
11. 赛后复盘结果
12. 球队管理页
13. 球员管理页
14. 排行榜页
15. AI 调用日志页
16. AI 日志详情弹窗
17. 报告中心页
18. 报告 Markdown 预览
19. 多页面导航跳转效果

---

## 23. 报告或答辩中的描述口径

可以在论文或答辩中这样描述：

```text
系统前端采用 HTML、CSS 和原生 JavaScript 实现，部署在 Spring Boot 的 src/main/resources/static 目录下。为避免单页面堆叠造成结构混乱，前端按照业务模块拆分为首页、比赛管理、比赛详情、球队管理、球员管理、排行榜、AI 日志和报告中心等多个页面。首页负责系统概览和功能入口，复杂业务集中在独立页面中完成。比赛详情页通过 URL 参数加载指定比赛，实现状态控制、事件录入、统计展示、AI 态势分析和赛后复盘；AI 日志页与报告中心页用于展示 AI Agent 调用过程和分析结果归档，从而增强系统的可追溯性和完整性。
```

---

## 24. 最终效果要求

完成后，前端应该从：

```text
一个页面展示全部功能的简单 Demo
```

变成：

```text
一个有首页、有导航、有详情页、有管理页、有排行榜、有 AI 日志、有报告中心的完整赛事数据分析系统
```

这样项目展示效果会明显更好，也更适合课程设计报告和答辩演示。
