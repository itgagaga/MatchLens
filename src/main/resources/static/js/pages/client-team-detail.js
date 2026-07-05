const ClientTeamDetailPage = {
  teamId: null,
  team: null,

  async init() {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    if (!token) { window.location.href = './login.html'; return; }
    if (role === 'ADMIN') { window.location.href = './matches.html'; return; }

    this.teamId = Common.getParam('teamId');
    if (!this.teamId) {
      document.querySelector('.page').innerHTML = '<div class="empty">缺少 teamId 参数</div>';
      return;
    }

    await this.loadTeam();
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('nickname');
    localStorage.removeItem('role');
    window.location.href = './login.html';
  },

  async loadTeam() {
    document.getElementById('teamBanner').innerHTML = '<div class="loading">加载中...</div>';
    try {
      const result = await API.clientGetTeam(this.teamId);
      this.team = result.data || result;
      this.renderBanner();
      this.renderPlayers();
    } catch (e) {
      document.getElementById('teamBanner').innerHTML = '<div class="empty" style="color:var(--danger)">加载球队信息失败</div>';
    }
  },

  renderBanner() {
    const t = this.team;
    const players = t.players || [];
    const totalScore = t.totalScore || players.reduce((sum, p) => sum + ((p.statistics || {}).SCORE || 0), 0);
    const avgAge = players.length > 0
      ? (players.reduce((s, p) => s + (p.age || 0), 0) / players.length).toFixed(1)
      : '-';
    const avgHeight = players.length > 0
      ? (players.reduce((s, p) => s + (p.height || 0), 0) / players.length).toFixed(0)
      : '-';

    document.getElementById('teamBanner').innerHTML = `
      <div class="ctd-banner">
        <div class="ctd-banner-main">
          <div class="ctd-banner-icon">🏅</div>
          <div class="ctd-banner-info">
            <h1 class="ctd-banner-name">${this.escapeHtml(t.teamName || '未命名球队')}</h1>
            <div class="ctd-banner-meta">
              ${t.city ? '📍 ' + this.escapeHtml(t.city) : ''}
              ${t.coachName ? ' · 🧑‍🏫 ' + this.escapeHtml(t.coachName) : ''}
            </div>
          </div>
        </div>
        <div class="ctd-banner-stats">
          <div class="ctd-stat-box">
            <div class="ctd-stat-num">${players.length}</div>
            <div class="ctd-stat-label">球员</div>
          </div>
          <div class="ctd-stat-box">
            <div class="ctd-stat-num">${totalScore}</div>
            <div class="ctd-stat-label">总得分</div>
          </div>
          <div class="ctd-stat-box">
            <div class="ctd-stat-num">${avgAge}</div>
            <div class="ctd-stat-label">平均年龄</div>
          </div>
          <div class="ctd-stat-box">
            <div class="ctd-stat-num">${avgHeight}${avgHeight !== '-' ? 'cm' : ''}</div>
            <div class="ctd-stat-label">平均身高</div>
          </div>
        </div>
      </div>
    `;
  },

  renderPlayers() {
    const players = this.team.players || [];
    const container = document.getElementById('playersContainer');

    if (!players.length) {
      container.innerHTML = '<div class="empty">暂无球员数据</div>';
      return;
    }

    // 按号码排序
    players.sort((a, b) => (a.number || 999) - (b.number || 999));

    // 找最高分球员
    let topScorerId = null;
    let topScore = 0;
    players.forEach(p => {
      const s = (p.statistics || {}).SCORE || 0;
      if (s > topScore) { topScore = s; topScorerId = p.playerId; }
    });

    container.innerHTML = `<div class="ctd-player-list">${players.map(p => this.renderPlayerDetail(p, p.playerId === topScorerId)).join('')}</div>`;
  },

  renderPlayerDetail(p, isTopScorer) {
    const stats = p.statistics || {};
    const score = stats.SCORE || 0;

    // 所有统计项
    const statEntries = Object.entries(stats).filter(([k]) => k !== 'SCORE');

    const bodyInfo = [
      p.age ? `${p.age}岁` : '',
      p.height ? `${p.height}cm` : '',
      p.weight ? `${p.weight}kg` : ''
    ].filter(Boolean).join(' · ');

    return `
      <div class="ctd-player-card ${isTopScorer ? 'ctd-top-scorer' : ''}">
        <div class="ctd-player-header">
          <div class="ctd-player-number">#${p.number || '-'}</div>
          <div class="ctd-player-main">
            <div class="ctd-player-name-row">
              <span class="ctd-player-name">${this.escapeHtml(p.playerName || '未知')}</span>
              ${isTopScorer ? '<span class="ctd-top-badge">🏆 最佳射手</span>' : ''}
              <span class="ctd-player-pos">${this.escapeHtml(p.position || '-')}</span>
            </div>
            <div class="ctd-player-body-info">${bodyInfo || '<span style="color:var(--text-muted)">暂无体格数据</span>'}</div>
          </div>
          <div class="ctd-player-score">${score}<span class="ctd-score-unit">分</span></div>
        </div>
        ${statEntries.length > 0 ? `
          <div class="ctd-player-stats">
            ${statEntries.map(([k, v]) => `
              <div class="ctd-stat-item">
                <div class="ctd-stat-item-val">${v}</div>
                <div class="ctd-stat-item-label">${this.statLabel(k)}</div>
              </div>
            `).join('')}
          </div>
        ` : ''}
      </div>
    `;
  },

  statLabel(key) {
    if (typeof STAT_KEY !== 'undefined' && STAT_KEY[key]) return STAT_KEY[key];
    const e = typeof EVENT_TYPE !== 'undefined' ? EVENT_TYPE[key] : null;
    return e ? e.label : key;
  },

  escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  }
};

document.addEventListener('DOMContentLoaded', () => ClientTeamDetailPage.init());
