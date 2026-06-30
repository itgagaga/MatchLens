const MatchesPage = {
  allMatches: [],

  async loadMatches() {
    const listEl = document.getElementById('matchList');
    Common.showLoading(listEl);
    try {
      const matches = await API.getMatches();
      this.allMatches = matches || [];
      this.renderMatches();
    } catch (e) {
      Common.showError(listEl, '加载比赛列表失败');
    }
  },

  renderMatches() {
    const listEl = document.getElementById('matchList');
    const sportFilter = document.getElementById('filterSportType').value;
    const statusFilter = document.getElementById('filterStatus').value;

    let filtered = this.allMatches;
    if (sportFilter) filtered = filtered.filter(m => m.sportType === sportFilter);
    if (statusFilter) filtered = filtered.filter(m => m.status === statusFilter);

    if (!filtered.length) {
      Common.showEmpty(listEl, '暂无比赛');
      return;
    }

    listEl.innerHTML = filtered.map(m => {
      const home = m.teamA ? m.teamA.teamName : '甲方';
      const away = m.teamB ? m.teamB.teamName : '乙方';
      const homeScore = m.statistics ? m.statistics.scoreA : 0;
      const awayScore = m.statistics ? m.statistics.scoreB : 0;
      return `
        <div class="match-card">
          <div style="display:flex;justify-content:space-between;align-items:center">
            <div class="match-title">${m.matchName || '未命名比赛'}</div>
            ${Common.statusBadge(m.status)}
          </div>
          <div style="margin:12px 0">
            <div class="match-vs">${home} vs ${away}</div>
            <div class="match-score" style="margin-top:8px">${homeScore} : ${awayScore}</div>
          </div>
          <div class="match-meta">
            ${Common.sportBadge(m.sportType)} · ${Common.formatTime(m.createTime)}
          </div>
          <div style="margin-top:12px;display:flex;gap:8px;flex-wrap:wrap">
            <a href="./match-detail.html?matchId=${m.matchId}" class="btn btn-sm">进入详情</a>
            <a href="./match-admin.html?matchId=${m.matchId}" class="btn btn-sm btn-orange">比赛设置</a>
          </div>
        </div>
      `;
    }).join('');
  },

  async createMatch() {
    const name = document.getElementById('matchName').value.trim();
    const type = document.getElementById('sportType').value;
    if (!name) {
      Common.toast('请输入比赛名称');
      return;
    }
    try {
      await API.createMatch({ matchName: name, sportType: type });
      Common.toast('创建成功');
      document.getElementById('matchName').value = '';
      await this.loadMatches();
    } catch (e) {
      Common.toast('创建失败: ' + e.message);
    }
  }
};

document.addEventListener('DOMContentLoaded', () => MatchesPage.loadMatches());
