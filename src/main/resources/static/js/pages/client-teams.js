const ClientTeamsPage = {
  teams: [],

  async init() {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    if (!token) { window.location.href = './login.html'; return; }
    if (role === 'ADMIN') { window.location.href = './matches.html'; return; }

    await this.loadTeams();
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('nickname');
    localStorage.removeItem('role');
    window.location.href = './login.html';
  },

  async loadTeams() {
    const container = document.getElementById('teamsContainer');
    container.innerHTML = '<div class="loading">加载中...</div>';

    try {
      this.teams = await API.clientGetTeams() || [];
      this.renderTeams(this.teams);
    } catch (e) {
      container.innerHTML = '<div class="empty" style="color:var(--danger)">加载球队列表失败</div>';
    }
  },

  renderTeams(teams) {
    const container = document.getElementById('teamsContainer');
    if (!teams || !teams.length) {
      container.innerHTML = '<div class="empty">暂无球队数据</div>';
      return;
    }

    container.innerHTML = '<div class="ct-team-grid">' + teams.map(team => {
      const players = team.players || [];
      const playerCount = players.length;
      const totalScore = team.totalScore || 0;

      return `
        <a class="ct-team-card" href="./client-team-detail.html?teamId=${team.teamId}">
          <div class="ct-team-header">
            <div class="ct-team-info">
              <div class="ct-team-name">🏅 ${this.escapeHtml(team.teamName || '未命名球队')}</div>
              <div class="ct-team-meta">
                ${team.city ? '📍 ' + this.escapeHtml(team.city) : ''}
                ${team.coachName ? ' · 🧑‍🏫 ' + this.escapeHtml(team.coachName) : ''}
              </div>
            </div>
            <div class="ct-team-right">
              <span class="ct-player-count">${playerCount} 名球员</span>
              <span class="ct-arrow">→</span>
            </div>
          </div>
          ${totalScore > 0 ? `<div class="ct-team-score">总得分 ${totalScore}</div>` : ''}
        </a>
      `;
    }).join('') + '</div>';
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

document.addEventListener('DOMContentLoaded', () => ClientTeamsPage.init());
