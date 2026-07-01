const TeamsPage = {
  teams: [],

  async loadTeams() {
    const listEl = document.getElementById('teamList');
    Common.showLoading(listEl);
    try {
      this.teams = await API.getTeams() || [];
      this.renderTeams();
    } catch (e) {
      Common.showError(listEl, '该功能需要后端接口支持');
    }
  },

  renderTeams() {
    const listEl = document.getElementById('teamList');
    if (!this.teams.length) { Common.showEmpty(listEl, '暂无球队'); return; }
    listEl.innerHTML = this.teams.map(t => `
      <div class="team-card" style="margin-bottom:12px">
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div class="team-title">${t.teamName || '-'}</div>
          <span class="badge badge-blue">球队</span>
        </div>
        <div class="team-meta">城市: ${t.city || '-'} · 教练: ${t.coachName || '-'}</div>
        <div class="action-group" style="margin-top:10px">
          <button class="btn btn-sm btn-green" onclick="TeamsPage.viewPlayers('${t.teamId}')">查看球员</button>
          <button class="btn btn-sm btn-orange" onclick="TeamsPage.editTeam('${t.teamId}')">编辑</button>
          <button class="btn btn-sm btn-red" onclick="TeamsPage.deleteTeam('${t.teamId}')">删除</button>
          <a class="btn btn-sm" href="./players.html?teamId=${t.teamId}">球员管理</a>
        </div>
      </div>
    `).join('');
  },

  async saveTeam() {
    const id = document.getElementById('editTeamId').value;
    const data = {
      teamName: document.getElementById('teamName').value.trim(),
      city: document.getElementById('teamCity').value.trim(),
      coachName: document.getElementById('teamCoach').value.trim()
    };
    if (!data.teamName) { Common.toast('请输入球队名称'); return; }
    try {
      if (id) {
        await API.updateTeam(id, data);
        Common.toast('更新成功');
      } else {
        await API.createTeam(data);
        Common.toast('创建成功');
      }
      this.resetForm();
      await this.loadTeams();
    } catch (e) { Common.toast('操作失败: ' + e.message); }
  },

  editTeam(teamId) {
    const t = this.teams.find(x => x.teamId === teamId);
    if (!t) return;
    document.getElementById('editTeamId').value = t.teamId;
    document.getElementById('teamName').value = t.teamName || '';
    document.getElementById('teamCity').value = t.city || '';
    document.getElementById('teamCoach').value = t.coachName || '';
    document.getElementById('teamFormTitle').textContent = '编辑球队';
  },

  resetForm() {
    document.getElementById('editTeamId').value = '';
    document.getElementById('teamName').value = '';
    document.getElementById('teamCity').value = '';
    document.getElementById('teamCoach').value = '';
    document.getElementById('teamFormTitle').textContent = '新增球队';
  },

  async deleteTeam(teamId) {
    if (!confirm('确认删除该球队？')) return;
    try {
      await API.deleteTeam(teamId);
      Common.toast('删除成功');
      await this.loadTeams();
    } catch (e) { Common.toast('删除失败'); }
  },

  async viewPlayers(teamId) {
    const el = document.getElementById('playerModalContent');
    el.innerHTML = '<div class="loading">加载中...</div>';
    Common.openModal('playerModal');
    try {
      const players = await API.getTeamPlayers(teamId);
      const list = players || [];
      if (!list.length) { el.innerHTML = '<div class="empty">暂无球员</div>'; return; }
      el.innerHTML = '<table class="table"><thead><tr><th>姓名</th><th>号码</th><th>位置</th></tr></thead><tbody>' +
        list.map(p => `<tr><td>${p.playerName}</td><td>${p.number}</td><td>${p.position || '-'}</td></tr>`).join('') +
        '</tbody></table>';
    } catch (e) { el.innerHTML = '<div class="empty">该功能需要后端接口支持</div>'; }
  }
};

document.addEventListener('DOMContentLoaded', () => TeamsPage.loadTeams());
