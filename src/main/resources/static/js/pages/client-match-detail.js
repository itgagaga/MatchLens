const ClientMatchDetailPage = {
  matchId: null,
  match: null,

  async init() {
    // 检查 USER 登录状态
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    if (!token) {
      window.location.href = './login.html';
      return;
    }
    if (role === 'ADMIN') {
      // 管理员跳转到管理端
      window.location.href = './matches.html';
      return;
    }

    this.matchId = Common.getParam('matchId');
    if (!this.matchId) {
      document.querySelector('.page').innerHTML = '<div class="empty">缺少 matchId 参数</div>';
      return;
    }

    await this.loadMatch();
    await this.loadEvents();
    await this.loadStatistics();
    await this.loadReports();
    await this.loadQaHistory();
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('nickname');
    localStorage.removeItem('role');
    window.location.href = './login.html';
  },

  toast(msg) {
    const el = document.getElementById('toast');
    el.textContent = msg;
    el.classList.add('show');
    clearTimeout(el._timer);
    el._timer = setTimeout(() => el.classList.remove('show'), 2500);
  },

  async loadMatch() {
    try {
      this.match = await API.clientGetMatch(this.matchId);
      if (!this.match) {
        this.toast('比赛不存在');
        return;
      }
      this.renderScoreBoard();
      this.renderMatchInfo();
      // 设置阵容链接
      document.getElementById('rosterLink').href = './client-rosters.html?matchId=' + this.matchId;
    } catch (e) {
      this.toast('加载比赛失败');
    }
  },

  renderScoreBoard() {
    const m = this.match;
    const home = m.teamA ? m.teamA.teamName : '甲方';
    const away = m.teamB ? m.teamB.teamName : '乙方';
    const homeScore = m.statistics ? m.statistics.scoreA : 0;
    const awayScore = m.statistics ? m.statistics.scoreB : 0;
    document.getElementById('scoreBoard').innerHTML = `
      <section class="score-hero">
        <div class="team-name">${home}</div>
        <div class="score-number">
          <span>${homeScore}</span>
          <span>:</span>
          <span>${awayScore}</span>
        </div>
        <div class="team-name">${away}</div>
      </section>
    `;
  },

  renderMatchInfo() {
    const m = this.match;
    document.getElementById('matchInfo').innerHTML = `
      <div class="match-info-item"><div class="info-label">比赛名称</div><div class="info-value">${m.matchName || '-'}</div></div>
      <div class="match-info-item"><div class="info-label">赛事类型</div><div class="info-value">${Common.sportBadge(m.sportType)}</div></div>
      <div class="match-info-item"><div class="info-label">比赛状态</div><div class="info-value">${Common.statusBadge(m.status)}</div></div>
      <div class="match-info-item"><div class="info-label">创建时间</div><div class="info-value">${Common.formatTime(m.createTime)}</div></div>
    `;
  },

  async loadEvents() {
    const el = document.getElementById('eventTimeline');
    Common.showLoading(el);
    try {
      const result = await API.clientGetMatchEvents(this.matchId);
      const events = Array.isArray(result) ? result : (result && result.data ? result.data : []);
      if (!events.length) { Common.showEmpty(el, '暂无事件'); return; }
      el.innerHTML = '<div class="timeline">' + events.map(e => `
        <div class="timeline-item">
          <div class="event-time">${Common.formatTime(e.eventTime)} ${Common.eventBadge(e.eventType)}</div>
          <div class="event-desc">${e.description || ''} ${e.scoreValue ? '(' + e.scoreValue + '分)' : ''}</div>
        </div>
      `).join('') + '</div>';
    } catch (e) { Common.showEmpty(el, '加载事件失败'); }
  },

  async loadStatistics() {
    const el = document.getElementById('statisticsPanel');
    try {
      const stats = await API.clientGetStatistics(this.matchId);
      if (!stats) {
        Common.showEmpty(el, '暂无统计数据');
        return;
      }
      const m = this.match;
      const homeName = m.teamA ? m.teamA.teamName : '甲方';
      const awayName = m.teamB ? m.teamB.teamName : '乙方';
      const scoreA = stats.scoreA || 0;
      const scoreB = stats.scoreB || 0;
      const total = scoreA + scoreB;
      const pctA = total > 0 ? Math.round((scoreA / total) * 100) : 50;
      const pctB = total > 0 ? 100 - pctA : 50;
      const eventCount = stats.eventCount || 0;
      const leading = stats.leadingTeam || '平局';
      const scoreDiff = stats.scoreDifference || 0;

      let html = `
        <div class="stat-score-card">
          <div class="stat-score-row">
            <div class="stat-team-col">
              <div class="stat-team-label home">${homeName}</div>
              <div class="stat-team-score home">${scoreA}</div>
            </div>
            <div class="stat-vs">VS</div>
            <div class="stat-team-col">
              <div class="stat-team-label away">${awayName}</div>
              <div class="stat-team-score away">${scoreB}</div>
            </div>
          </div>
          <div class="stat-bar-wrap">
            <div class="stat-bar-home" style="width:${pctA}%"></div>
            <div class="stat-bar-away" style="width:${pctB}%"></div>
          </div>
          <div class="stat-bar-labels">
            <span>${pctA}%</span>
            <span>得分占比</span>
            <span>${pctB}%</span>
          </div>
        </div>`;

      html += `
        <div class="stat-summary-row">
          <div class="stat-mini-card">
            <div class="stat-mini-num">${eventCount}</div>
            <div class="stat-mini-label">事件总数</div>
          </div>
          <div class="stat-mini-card">
            <div class="stat-mini-num accent">${scoreDiff}</div>
            <div class="stat-mini-label">分差</div>
          </div>
          <div class="stat-mini-card">
            <div class="stat-mini-num ${leading === '平局' ? '' : 'warm'}">${leading}</div>
            <div class="stat-mini-label">领先方</div>
          </div>
        </div>`;

      const teamStats = stats.teamStats || {};
      const homePrefix = homeName + '_';
      const awayPrefix = awayName + '_';
      const statKeys = new Set();
      Object.keys(teamStats).forEach(k => {
        if (k.startsWith(homePrefix)) statKeys.add(k.replace(homePrefix, ''));
        else if (k.startsWith(awayPrefix)) statKeys.add(k.replace(awayPrefix, ''));
      });

      if (statKeys.size > 0) {
        html += '<div class="stat-detail-section"><h3 class="stat-section-title">各项数据对比</h3>';
        statKeys.forEach(key => {
          const vA = teamStats[homePrefix + key] || 0;
          const vB = teamStats[awayPrefix + key] || 0;
          const maxVal = Math.max(vA, vB, 1);
          const barPctA = Math.max((vA / maxVal) * 100, 4);
          const barPctB = Math.max((vB / maxVal) * 100, 4);
          html += `
            <div class="stat-compare-row">
              <div class="stat-compare-val home">${vA}</div>
              <div class="stat-compare-bars">
                <div class="stat-compare-bar-wrap left">
                  <div class="stat-compare-bar home" style="width:${barPctA}%"></div>
                </div>
                <div class="stat-compare-key">${Common.eventTypeName ? Common.eventTypeName(key) : key}</div>
                <div class="stat-compare-bar-wrap right">
                  <div class="stat-compare-bar away" style="width:${barPctB}%"></div>
                </div>
              </div>
              <div class="stat-compare-val away">${vB}</div>
            </div>`;
        });
        html += '</div>';
      }

      el.innerHTML = html;
    } catch (e) { Common.showEmpty(el, '加载统计失败'); }
  },

  async loadReports() {
    try {
      const reports = await API.clientGetMatchReports(this.matchId);
      if (!reports || !reports.length) {
        document.getElementById('latestAnalysis').innerHTML = '<div class="empty" style="color:var(--text-secondary)">暂无态势分析报告</div>';
        document.getElementById('latestReview').innerHTML = '<div class="empty" style="color:var(--text-secondary)">暂无复盘报告</div>';
        return;
      }

      // 找最新的态势分析和复盘报告
      let latestAnalysis = null;
      let latestReview = null;
      for (const r of reports) {
        if (r.reportType === 'SITUATION' && !latestAnalysis) latestAnalysis = r;
        if (r.reportType === 'REVIEW' && !latestReview) latestReview = r;
      }

      const analysisEl = document.getElementById('latestAnalysis');
      if (latestAnalysis) {
        analysisEl.innerHTML = Common.markdownToHtml(latestAnalysis.content);
        analysisEl.style.display = 'block';
      } else {
        analysisEl.innerHTML = '<div class="empty" style="color:var(--text-secondary)">暂无态势分析报告</div>';
      }

      const reviewEl = document.getElementById('latestReview');
      if (latestReview) {
        reviewEl.innerHTML = Common.markdownToHtml(latestReview.content);
        reviewEl.style.display = 'block';
      } else {
        reviewEl.innerHTML = '<div class="empty" style="color:var(--text-secondary)">暂无复盘报告</div>';
      }
    } catch (e) {
      document.getElementById('latestAnalysis').innerHTML = '<div class="empty" style="color:var(--danger)">加载报告失败</div>';
      document.getElementById('latestReview').innerHTML = '<div class="empty" style="color:var(--danger)">加载报告失败</div>';
    }
  },

  // ===== AI 问答助手 =====
  qaStreaming: false,

  async loadQaHistory() {
    try {
      const history = await API.getQaHistory(this.matchId);
      const messages = Array.isArray(history) ? history : (history && history.data ? history.data : []);
      if (!messages.length) return;

      // 隐藏欢迎信息
      const welcome = document.querySelector('.qa-welcome');
      if (welcome) welcome.style.display = 'none';

      // 渲染历史消息
      for (const msg of messages) {
        this.appendQaMessage(msg.role, msg.content);
      }
    } catch (e) {
      // 历史记录加载失败不影响页面使用
      console.warn('加载问答历史失败:', e);
    }
  },

  async clearQaHistory() {
    if (!confirm('确定清空所有问答记录？')) return;
    try {
      await API.clearQaHistory(this.matchId);
      // 清空 DOM
      const messagesEl = document.getElementById('qaMessages');
      messagesEl.innerHTML = `
        <div class="qa-welcome">
          <div class="qa-welcome-icon">🤖</div>
          <div class="qa-welcome-text">您好！我是智能赛事助手</div>
          <div class="qa-welcome-hint">您可以问我关于这场比赛的任何问题</div>
          <div class="qa-quick-questions">
            <button class="qa-quick-btn" onclick="ClientMatchDetailPage.askQuickQuestion('这场比赛目前比分是多少？')">目前比分</button>
            <button class="qa-quick-btn" onclick="ClientMatchDetailPage.askQuickQuestion('哪位球员表现最出色？')">最佳球员</button>
            <button class="qa-quick-btn" onclick="ClientMatchDetailPage.askQuickQuestion('比赛中有哪些关键时刻？')">关键时刻</button>
            <button class="qa-quick-btn" onclick="ClientMatchDetailPage.askQuickQuestion('双方的优势和劣势分别是什么？')">优劣势分析</button>
          </div>
        </div>`;
      this.toast('问答记录已清空');
    } catch (e) {
      this.toast('清空失败');
    }
  },

  askQuickQuestion(question) {
    document.getElementById('qaInput').value = question;
    this.askQuestion();
  },

  askQuestion() {
    const input = document.getElementById('qaInput');
    const question = input.value.trim();
    if (!question || this.qaStreaming) return;

    // 隐藏欢迎信息
    const welcome = document.querySelector('.qa-welcome');
    if (welcome) welcome.style.display = 'none';

    // 添加用户消息气泡
    this.appendQaMessage('user', question);
    input.value = '';

    // 开始流式响应
    this.streamQaResponse(question);
  },

  appendQaMessage(role, content) {
    const messagesEl = document.getElementById('qaMessages');
    const msgDiv = document.createElement('div');
    msgDiv.className = `qa-msg ${role}`;

    const avatar = role === 'ai' ? '🤖' : '👤';
    const bubbleContent = role === 'ai'
      ? Common.markdownToHtml(content)
      : `<span>${this.escapeHtml(content)}</span>`;

    msgDiv.innerHTML = `
      <div class="qa-avatar">${avatar}</div>
      <div class="qa-bubble">${bubbleContent}</div>
    `;
    messagesEl.appendChild(msgDiv);
    messagesEl.scrollTop = messagesEl.scrollHeight;
    return msgDiv;
  },

  appendQaTyping() {
    const messagesEl = document.getElementById('qaMessages');
    const msgDiv = document.createElement('div');
    msgDiv.className = 'qa-msg ai';
    msgDiv.id = 'qaTypingMsg';
    msgDiv.innerHTML = `
      <div class="qa-avatar">🤖</div>
      <div class="qa-bubble">
        <div class="qa-typing"><span></span><span></span><span></span></div>
      </div>
    `;
    messagesEl.appendChild(msgDiv);
    messagesEl.scrollTop = messagesEl.scrollHeight;
    return msgDiv;
  },

  removeQaTyping() {
    const el = document.getElementById('qaTypingMsg');
    if (el) el.remove();
  },

  streamQaResponse(question) {
    this.qaStreaming = true;
    const sendBtn = document.getElementById('qaSendBtn');
    sendBtn.disabled = true;
    sendBtn.textContent = '回复中...';

    this.appendQaTyping();

    const url = API.streamQa(this.matchId, question);
    const eventSource = new EventSource(url);
    let collected = '';
    let aiBubble = null;

    eventSource.addEventListener('start', () => {
      this.removeQaTyping();
      const msg = this.appendQaMessage('ai', '');
      aiBubble = msg.querySelector('.qa-bubble');
      aiBubble.innerHTML = '<span class="stream-cursor"></span>';
    });

    eventSource.addEventListener('chunk', (e) => {
      collected += e.data;
      if (aiBubble) {
        aiBubble.innerHTML = Common.markdownToHtml(collected) + '<span class="stream-cursor"></span>';
        const messagesEl = document.getElementById('qaMessages');
        messagesEl.scrollTop = messagesEl.scrollHeight;
      }
    });

    eventSource.addEventListener('done', () => {
      eventSource.close();
      this.qaStreaming = false;
      sendBtn.disabled = false;
      sendBtn.textContent = '发送';
      if (aiBubble) {
        aiBubble.innerHTML = Common.markdownToHtml(collected);
      }
    });

    eventSource.addEventListener('error', (e) => {
      eventSource.close();
      this.removeQaTyping();
      this.qaStreaming = false;
      sendBtn.disabled = false;
      sendBtn.textContent = '发送';
      const errorMsg = e.data || '连接中断，请重试';
      this.appendQaMessage('ai', '❌ ' + errorMsg);
    });

    eventSource.onerror = () => {
      eventSource.close();
      this.removeQaTyping();
      this.qaStreaming = false;
      sendBtn.disabled = false;
      sendBtn.textContent = '发送';
      if (!collected) {
        this.appendQaMessage('ai', '❌ 连接失败，请检查网络后重试');
      }
    };
  },

  escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
};

document.addEventListener('DOMContentLoaded', () => ClientMatchDetailPage.init());
