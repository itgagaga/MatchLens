const ClientRostersPage = {
  matchId: null,
  match: null,

  async init() {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    if (!token) { window.location.href = './login.html'; return; }
    if (role === 'ADMIN') { window.location.href = './matches.html'; return; }

    this.matchId = Common.getParam('matchId');
    if (!this.matchId) {
      document.querySelector('.page').innerHTML = '<div class="empty">缺少 matchId 参数</div>';
      return;
    }

    // 返回链接
    document.getElementById('backLink').href = './client-match-detail.html?matchId=' + this.matchId;

    await this.loadMatch();
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
      if (!this.match) { this.toast('比赛不存在'); return; }
      this.renderMatchHeader();
      this.renderTeam('home');
      this.renderTeam('away');
    } catch (e) {
      this.toast('加载比赛失败');
    }
  },

  renderMatchHeader() {
    const m = this.match;
    const home = m.teamA ? m.teamA.teamName : '甲方';
    const away = m.teamB ? m.teamB.teamName : '乙方';
    const scoreA = m.statistics ? m.statistics.scoreA : 0;
    const scoreB = m.statistics ? m.statistics.scoreB : 0;

    document.getElementById('breadcrumb').textContent =
      (m.matchName || '未命名比赛') + ' / 球队阵容';

    document.getElementById('matchHeader').innerHTML = `
      <div class="roster-match-banner">
        <div class="roster-banner-team">
          <div class="roster-banner-name">${this.escapeHtml(home)}</div>
          <div class="roster-banner-score home">${scoreA}</div>
        </div>
        <div class="roster-banner-vs">VS</div>
        <div class="roster-banner-team">
          <div class="roster-banner-name">${this.escapeHtml(away)}</div>
          <div class="roster-banner-score away">${scoreB}</div>
        </div>
      </div>
      <div class="roster-match-meta">
        ${Common.sportBadge(m.sportType)} · ${Common.statusBadge(m.status)} · ${Common.formatTime(m.createTime)}
      </div>
    `;
  },

  renderTeam(side) {
    const m = this.match;
    const team = side === 'home' ? m.teamA : m.teamB;
    const headerEl = document.getElementById(side === 'home' ? 'homeTeamHeader' : 'awayTeamHeader');
    const playersEl = document.getElementById(side === 'home' ? 'homePlayers' : 'awayPlayers');
    const sectionEl = document.getElementById(side === 'home' ? 'homeTeamSection' : 'awayTeamSection');

    const teamName = team ? team.teamName : (side === 'home' ? '甲方' : '乙方');
    const players = (team && team.players) ? team.players : [];
    const teamScore = m.statistics ? (side === 'home' ? m.statistics.scoreA : m.statistics.scoreB) : 0;

    // 队伍标题
    headerEl.innerHTML = `
      <div class="roster-section-header ${side}">
        <div>
          <span class="roster-section-icon">${side === 'home' ? '🔵' : '🟠'}</span>
          <span class="roster-section-name">${this.escapeHtml(teamName)}</span>
        </div>
        <div class="roster-section-stats">
          <span class="roster-section-count">${players.length} 名球员</span>
          <span class="roster-section-score">${teamScore} 分</span>
        </div>
      </div>
    `;

    if (!players.length) {
      playersEl.innerHTML = '<div class="empty" style="padding:32px 0;color:var(--text-muted)">暂无球员数据</div>';
      return;
    }

    // 按得分排序
    const sorted = [...players].sort((a, b) => {
      const sa = (a.statistics || {}).SCORE || 0;
      const sb = (b.statistics || {}).SCORE || 0;
      return sb - sa;
    });

    // 找到最高得分球员
    const topScorer = sorted.length > 0 ? ((sorted[0].statistics || {}).SCORE || 0) : 0;

    playersEl.innerHTML = sorted.map((p, idx) => {
      const stats = p.statistics || {};
      const score = stats.SCORE || 0;
      const isTop = score > 0 && score === topScorer;

      // 构建统计数据标签
      const statBadges = Object.entries(stats)
        .filter(([k, v]) => v > 0)
        .map(([k, v]) => {
          const label = Common.eventTypeName ? Common.eventTypeName(k) : k;
          const isActive = k === 'SCORE';
          return `<span class="player-stat-chip ${isActive ? 'active' : ''}">${label} <b>${v}</b></span>`;
        })
        .join('');

      // 得分占比条
      const scorePct = teamScore > 0 ? Math.min((score / teamScore) * 100, 100) : 0;

      return `
        <div class="player-card ${isTop ? 'top-scorer' : ''}">
          <div class="player-card-top">
            <div class="player-number">#${p.number}</div>
            <div class="player-info">
              <div class="player-name">
                ${this.escapeHtml(p.playerName)}
                ${isTop ? '<span class="top-badge">👑 MVP</span>' : ''}
              </div>
              <div class="player-meta">
                ${p.position ? '<span>' + this.escapeHtml(p.position) + '</span>' : ''}
                ${p.age ? '<span>' + p.age + '岁</span>' : ''}
                ${p.height ? '<span>' + p.height + 'cm</span>' : ''}
                ${p.weight ? '<span>' + p.weight + 'kg</span>' : ''}
              </div>
            </div>
            <div class="player-score-big">${score}</div>
          </div>
          ${scorePct > 0 ? `
          <div class="player-score-bar-wrap">
            <div class="player-score-bar" style="width:${scorePct}%"></div>
            <span class="player-score-pct">占球队 ${Math.round(scorePct)}%</span>
          </div>` : ''}
          <div class="player-stats-row">
            ${statBadges || '<span style="color:var(--text-muted);font-size:12px">暂无数据</span>'}
          </div>
        </div>
      `;
    }).join('');
  },

  escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
};

document.addEventListener('DOMContentLoaded', () => ClientRostersPage.init());
