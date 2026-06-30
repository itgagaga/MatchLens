const MatchDetailPage = {
  matchId: null,
  match: null,

  async init() {
    this.matchId = Common.getParam('matchId');
    if (!this.matchId) {
      document.querySelector('.page').innerHTML = '<div class="empty">缺少 matchId 参数</div>';
      return;
    }
    document.getElementById('linkAdmin').href = `./match-admin.html?matchId=${this.matchId}`;
    document.getElementById('linkAiLogs').href = `./ai-logs.html?matchId=${this.matchId}`;
    document.getElementById('linkReports').href = `./reports.html?matchId=${this.matchId}`;
    await this.loadMatch();
    await this.loadEvents();
  },

  async loadMatch() {
    try {
      this.match = await API.getMatch(this.matchId);
      if (!this.match) {
        Common.toast('比赛不存在');
        return;
      }
      this.renderScoreBoard();
      this.renderMatchInfo();
      this.renderTeamSelect();
    } catch (e) {
      Common.toast('加载比赛失败');
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

  renderTeamSelect() {
    const m = this.match;
    const teamSelect = document.getElementById('eventTeam');
    const homeName = m.teamA ? m.teamA.teamName : null;
    const awayName = m.teamB ? m.teamB.teamName : null;
    let html = '<option value="">-- 请选择队伍 --</option>';
    if (m.teamA) html += `<option value="${m.teamA.teamId}">${homeName}（甲方）</option>`;
    if (m.teamB) html += `<option value="${m.teamB.teamId}">${awayName}（乙方）</option>`;
    teamSelect.innerHTML = html;
    document.getElementById('eventPlayer').innerHTML = '<option value="">-- 请先选择队伍 --</option>';
  },

  onTeamChange() {
    const teamId = document.getElementById('eventTeam').value;
    const playerSelect = document.getElementById('eventPlayer');
    if (!teamId) {
      playerSelect.innerHTML = '<option value="">-- 请先选择队伍 --</option>';
      return;
    }
    const m = this.match;
    let players = [];
    if (m.teamA && m.teamA.teamId === teamId) players = m.teamA.players || [];
    else if (m.teamB && m.teamB.teamId === teamId) players = m.teamB.players || [];
    if (!players.length) {
      playerSelect.innerHTML = '<option value="">该队伍暂无球员</option>';
      return;
    }
    playerSelect.innerHTML = '<option value="">-- 请选择球员 --</option>'
      + players.map(p => `<option value="${p.playerId}">${p.playerName}${p.number ? ' (#' + p.number + ')' : ''}</option>`).join('');
  },

  async recordEvent() {
    const teamId = document.getElementById('eventTeam').value;
    const playerId = document.getElementById('eventPlayer').value;
    const eventType = document.getElementById('eventType').value;
    const scoreValue = parseInt(document.getElementById('eventScoreValue').value) || 0;
    const description = document.getElementById('eventDescription').value.trim();
    if (!teamId) { Common.toast('请选择队伍'); return; }
    if (!playerId) { Common.toast('请选择球员'); return; }
    try {
      await API.recordEvent(this.matchId, { teamId, playerId, eventType, scoreValue, description });
      Common.toast('事件录入成功');
      document.getElementById('eventDescription').value = '';
      document.getElementById('eventScoreValue').value = '0';
      await this.loadMatch();
      await this.loadEvents();
      await this.loadStatistics();
    } catch (e) { Common.toast('录入失败: ' + e.message); }
  },

  async loadEvents() {
    const el = document.getElementById('eventTimeline');
    Common.showLoading(el);
    try {
      const result = await API.getMatchEvents(this.matchId);
      const events = Array.isArray(result) ? result : (result && result.data ? result.data : []);
      if (!events.length) { Common.showEmpty(el, '暂无事件'); return; }
      el.innerHTML = '<div class="timeline">' + events.map(e => `
        <div class="timeline-item">
          <div class="event-time">${Common.formatTime(e.eventTime)} ${Common.eventBadge(e.eventType)}</div>
          <div class="event-desc">${e.description || ''} ${e.scoreValue ? '(' + e.scoreValue + '分)' : ''}</div>
          <div class="event-actions">
            <button class="btn btn-sm btn-red" onclick="MatchDetailPage.deleteEvent('${e.eventId}')">删除</button>
          </div>
        </div>
      `).join('') + '</div>';
    } catch (e) { Common.showEmpty(el, '加载事件失败'); }
  },

  async deleteEvent(eventId) {
    if (!confirm('确认删除该事件？')) return;
    try {
      await API.deleteEvent(eventId);
      Common.toast('删除成功');
      await this.loadMatch();
      await this.loadEvents();
      await this.loadStatistics();
    } catch (e) { Common.toast('删除失败'); }
  },

  async loadStatistics() {
    const el = document.getElementById('statisticsPanel');
    try {
      const stats = await API.getStatistics(this.matchId);
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

      // --- 1. 比分对比条 ---
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

      // --- 2. 摘要指标卡片 ---
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

      // --- 3. 各队分类统计（横向条形图）---
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
    } catch (e) { Common.showEmpty(el, '加载统计失败: ' + e.message); }
  },

  async getAnalysis() {
    const el = document.getElementById('analysisResult');
    el.style.display = 'block';
    el.textContent = '正在生成态势分析...';
    try {
      const text = await API.getAnalysis(this.matchId);
      el.innerHTML = Common.markdownToHtml(text);
    } catch (e) { el.textContent = '生成失败: ' + e.message; }
  },

  async getReviewReport() {
    const el = document.getElementById('reportResult');
    el.style.display = 'block';
    el.textContent = '正在生成赛后复盘...';
    try {
      const text = await API.getReviewReport(this.matchId);
      el.innerHTML = Common.markdownToHtml(text);
    } catch (e) { el.textContent = '生成失败: ' + e.message; }
  },

  streamAnalysis() {
    const el = document.getElementById('analysisResult');
    el.style.display = 'block';
    el.textContent = '';
    const url = API.streamAnalysis(this.matchId);
    const evtSource = new EventSource(url);
    evtSource.addEventListener('chunk', e => { el.textContent += e.data; });
    evtSource.addEventListener('done', () => { evtSource.close(); el.innerHTML = Common.markdownToHtml(el.textContent); });
    evtSource.addEventListener('error', e => { evtSource.close(); if (el.textContent) el.innerHTML = Common.markdownToHtml(el.textContent); else el.textContent = '生成失败'; });
  },

  streamReport() {
    const el = document.getElementById('reportResult');
    el.style.display = 'block';
    el.textContent = '';
    const url = API.streamReport(this.matchId);
    const evtSource = new EventSource(url);
    evtSource.addEventListener('chunk', e => { el.textContent += e.data; });
    evtSource.addEventListener('done', () => { evtSource.close(); el.innerHTML = Common.markdownToHtml(el.textContent); });
    evtSource.addEventListener('error', e => { evtSource.close(); if (el.textContent) el.innerHTML = Common.markdownToHtml(el.textContent); else el.textContent = '生成失败'; });
  }
};

document.addEventListener('DOMContentLoaded', () => {
  MatchDetailPage.init().then(() => MatchDetailPage.loadStatistics());
});
