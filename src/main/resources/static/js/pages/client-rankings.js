const ClientRankingsPage = {
  matches: [],

  async init() {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    if (!token) { window.location.href = './login.html'; return; }
    if (role === 'ADMIN') { window.location.href = './matches.html'; return; }

    // 加载比赛列表供选择（使用客户端接口）
    try {
      this.matches = await API.clientQueryMatches() || [];
      const sel = document.getElementById('rankMatchId');
      sel.innerHTML = '<option value="">请选择比赛</option>' +
        this.matches.map(m => `<option value="${m.matchId}">${m.matchName || m.homeTeamName + ' vs ' + m.awayTeamName || '未命名比赛'}</option>`).join('');
    } catch (e) { this.matches = []; }

    document.getElementById('rankMode').addEventListener('change', function () {
      document.getElementById('rankMatchId').style.display = this.value === 'match' ? '' : 'none';
    });

    await this.load();
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('nickname');
    localStorage.removeItem('role');
    window.location.href = './login.html';
  },

  async load() {
    const listEl = document.getElementById('rankList');
    Common.showLoading(listEl);
    const mode = document.getElementById('rankMode').value;
    const matchId = document.getElementById('rankMatchId').value;
    const statKey = document.getElementById('rankStatKey').value;

    document.getElementById('rankTitle').textContent =
      (mode === 'overall' ? '全局' : '单场') + STAT_KEY[statKey] + '排行榜';

    try {
      let data;
      if (mode === 'overall') {
        data = await API.getOverallPlayerRankings({ statKey, limit: 20 });
      } else {
        if (!matchId) { Common.showEmpty(listEl, '请选择比赛'); return; }
        data = await API.getPlayerRankings({ matchId, statKey, limit: 20 });
      }
      const list = data || [];
      if (!list.length) { Common.showEmpty(listEl, '暂无排行数据'); return; }
      this.renderRankings(list, statKey);
    } catch (e) {
      Common.showError(listEl, '加载排行数据失败');
    }
  },

  renderRankings(list, statKey) {
    const listEl = document.getElementById('rankList');
    const medals = ['🥇', '🥈', '🥉'];
    listEl.innerHTML = list.map((item, i) => {
      const rank = i + 1;
      const rankClass = rank <= 3 ? `rank-${rank}` : '';
      const medal = rank <= 3 ? medals[i] : rank;
      return `
        <div class="rank-row ${rankClass}">
          <div class="rank-num">${medal}</div>
          <div class="rank-name">${item.playerName || '-'}</div>
          <div class="rank-value">${item.statValue ?? item[statKey.toLowerCase()] ?? 0}</div>
        </div>
      `;
    }).join('');
  }
};

document.addEventListener('DOMContentLoaded', () => ClientRankingsPage.init());
