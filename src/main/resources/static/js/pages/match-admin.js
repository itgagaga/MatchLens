const MatchAdminPage = {
  matchId: null,
  match: null,

  async init() {
    this.matchId = Common.getParam('matchId');
    if (!this.matchId) {
      document.querySelector('.page').innerHTML = '<div class="empty">缺少 matchId 参数</div>';
      return;
    }
    document.getElementById('linkDetail').href = `./match-detail.html?matchId=${this.matchId}`;
    document.getElementById('linkAiLogs').href = `./ai-logs.html?matchId=${this.matchId}`;
    document.getElementById('linkReports').href = `./reports.html?matchId=${this.matchId}`;
    await this.loadMatch();
  },

  async loadMatch() {
    try {
      this.match = await API.getMatch(this.matchId);
      if (!this.match) {
        Common.toast('比赛不存在');
        return;
      }
      this.renderMeta();
      this.renderPlayers();
    } catch (e) {
      Common.toast('加载比赛失败');
    }
  },

  renderMeta() {
    const m = this.match;
    document.getElementById('pageTitle').textContent = m.matchName || '比赛设置';
    document.getElementById('matchMeta').innerHTML =
      `${Common.sportBadge(m.sportType)} · ${Common.statusBadge(m.status)} · 创建于 ${Common.formatTime(m.createTime)}`;
    document.getElementById('matchName').value = m.matchName || '';
    if (m.teamA) document.getElementById('homeTeamName').value = m.teamA.teamName || '';
    if (m.teamB) document.getElementById('awayTeamName').value = m.teamB.teamName || '';
  },

  renderPlayers() {
    const m = this.match;
    const renderList = (players, containerId) => {
      const el = document.getElementById(containerId);
      if (!players || !players.length) {
        Common.showEmpty(el, '暂无球员');
        return;
      }
      el.innerHTML = '<table class="table"><thead><tr><th>姓名</th><th>号码</th></tr></thead><tbody>'
        + players.map(p => `<tr><td>${p.playerName}</td><td>${p.number || '-'}</td></tr>`).join('')
        + '</tbody></table>';
    };
    renderList(m.teamA ? m.teamA.players : [], 'homePlayersList');
    renderList(m.teamB ? m.teamB.players : [], 'awayPlayersList');
  },

  async updateMatchName() {
    const name = document.getElementById('matchName').value.trim();
    if (!name) { Common.toast('请输入比赛名称'); return; }
    try {
      await API.updateMatch(this.matchId, { matchName: name });
      Common.toast('名称已更新');
      await this.loadMatch();
    } catch (e) { Common.toast('更新失败: ' + e.message); }
  },

  async setTeams() {
    const home = document.getElementById('homeTeamName').value.trim();
    const away = document.getElementById('awayTeamName').value.trim();
    if (!home || !away) { Common.toast('请输入甲方和乙方名称'); return; }
    try {
      await API.setTeams(this.matchId, { teamAName: home, teamBName: away });
      Common.toast('队伍设置成功');
      await this.loadMatch();
    } catch (e) { Common.toast('设置失败: ' + e.message); }
  },

  async addPlayer() {
    const name = document.getElementById('playerName').value.trim();
    const num = parseInt(document.getElementById('playerNumber').value) || 0;
    const isHome = document.getElementById('playerHome').value === 'true';
    if (!name) { Common.toast('请输入球员姓名'); return; }
    try {
      await API.addPlayerToMatch(this.matchId, { playerName: name, number: num, teamA: isHome });
      Common.toast('球员添加成功');
      document.getElementById('playerName').value = '';
      document.getElementById('playerNumber').value = '';
      await this.loadMatch();
    } catch (e) { Common.toast('添加失败: ' + e.message); }
  },

  async changeStatus(action) {
    try {
      await API.changeMatchStatus(this.matchId, action);
      Common.toast('操作成功');
      await this.loadMatch();
    } catch (e) { Common.toast('操作失败: ' + e.message); }
  },

  async deleteMatch() {
    if (!confirm('确认删除该比赛？所有关联数据将被清除，此操作不可撤销！')) return;
    try {
      await API.deleteMatch(this.matchId);
      Common.toast('比赛已删除');
      setTimeout(() => window.location.href = './matches.html', 800);
    } catch (e) { Common.toast('删除失败: ' + e.message); }
  }
};

document.addEventListener('DOMContentLoaded', () => MatchAdminPage.init());
