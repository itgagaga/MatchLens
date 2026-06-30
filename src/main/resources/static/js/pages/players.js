const PlayersPage = {
  players: [],
  teams: [],

  async init() {
    await this.loadTeamsForSelect();
    await this.loadPlayers();
    // URL 参数 teamId
    const teamId = Common.getParam('teamId');
    if (teamId) {
      document.getElementById('filterTeamId').value = teamId;
      this.loadPlayers();
    }
  },

  async loadTeamsForSelect() {
    try {
      this.teams = await API.getTeams() || [];
    } catch (e) { this.teams = []; }
    const opts = '<option value="">全部球队</option>' + this.teams.map(t =>
      `<option value="${t.teamId}">${t.teamName}</option>`
    ).join('');
    document.getElementById('filterTeamId').innerHTML = opts;
    document.getElementById('playerTeamId').innerHTML = '<option value="">请选择球队</option>' +
      this.teams.map(t => `<option value="${t.teamId}">${t.teamName}</option>`).join('');
    // 位置选项
    document.getElementById('playerPosition').innerHTML = '<option value="">请选择</option>' +
      POSITION_OPTIONS.map(p => `<option value="${p}">${p}</option>`).join('');
  },

  async loadPlayers() {
    const listEl = document.getElementById('playerList');
    Common.showLoading(listEl);
    const teamId = document.getElementById('filterTeamId').value;
    try {
      const params = {};
      if (teamId) params.teamId = teamId;
      this.players = await API.getPlayers(params) || [];
      this.renderPlayers();
    } catch (e) {
      Common.showError(listEl, '该功能需要后端接口支持');
    }
  },

  renderPlayers() {
    const listEl = document.getElementById('playerList');
    if (!this.players.length) { Common.showEmpty(listEl, '暂无球员'); return; }
    listEl.innerHTML = `<table class="table"><thead><tr>
      <th>姓名</th><th>球队</th><th>号码</th><th>位置</th><th>年龄</th><th>身高</th><th>体重</th><th>操作</th>
    </tr></thead><tbody>${this.players.map(p => {
      const team = this.teams.find(t => t.teamId === p.teamId);
      return `<tr>
        <td style="font-weight:600">${p.playerName}</td>
        <td>${team ? team.teamName : p.teamId || '-'}</td>
        <td>${p.number}</td>
        <td>${p.position || '-'}</td>
        <td>${p.age || '-'}</td>
        <td>${p.height ? p.height + 'cm' : '-'}</td>
        <td>${p.weight ? p.weight + 'kg' : '-'}</td>
        <td>
          <div class="action-group">
            <button class="btn btn-sm btn-green" onclick="PlayersPage.viewDetail('${p.playerId}')">详情</button>
            <button class="btn btn-sm btn-orange" onclick="PlayersPage.editPlayer('${p.playerId}')">编辑</button>
            <button class="btn btn-sm btn-red" onclick="PlayersPage.deletePlayer('${p.playerId}')">删除</button>
          </div>
        </td>
      </tr>`;
    }).join('')}</tbody></table>`;
  },

  async savePlayer() {
    const id = document.getElementById('editPlayerId').value;
    const data = {
      playerName: document.getElementById('playerName').value.trim(),
      teamId: document.getElementById('playerTeamId').value || null,
      number: parseInt(document.getElementById('playerNumber').value) || 0,
      position: document.getElementById('playerPosition').value,
      age: parseInt(document.getElementById('playerAge').value) || null,
      height: parseInt(document.getElementById('playerHeight').value) || null,
      weight: parseInt(document.getElementById('playerWeight').value) || null
    };
    if (!data.playerName) { Common.toast('请输入球员姓名'); return; }
    try {
      if (id) {
        await API.updatePlayer(id, data);
        Common.toast('更新成功');
      } else {
        await API.createPlayer(data);
        Common.toast('创建成功');
      }
      this.resetForm();
      await this.loadPlayers();
    } catch (e) { Common.toast('操作失败: ' + e.message); }
  },

  editPlayer(playerId) {
    const p = this.players.find(x => x.playerId === playerId);
    if (!p) return;
    document.getElementById('editPlayerId').value = p.playerId;
    document.getElementById('playerName').value = p.playerName || '';
    document.getElementById('playerTeamId').value = p.teamId || '';
    document.getElementById('playerNumber').value = p.number || '';
    document.getElementById('playerPosition').value = p.position || '';
    document.getElementById('playerAge').value = p.age || '';
    document.getElementById('playerHeight').value = p.height || '';
    document.getElementById('playerWeight').value = p.weight || '';
    document.getElementById('playerFormTitle').textContent = '编辑球员';
  },

  resetForm() {
    document.getElementById('editPlayerId').value = '';
    document.getElementById('playerName').value = '';
    document.getElementById('playerTeamId').value = '';
    document.getElementById('playerNumber').value = '';
    document.getElementById('playerPosition').value = '';
    document.getElementById('playerAge').value = '';
    document.getElementById('playerHeight').value = '';
    document.getElementById('playerWeight').value = '';
    document.getElementById('playerFormTitle').textContent = '新增球员';
  },

  async deletePlayer(playerId) {
    if (!confirm('确认删除该球员？')) return;
    try {
      await API.deletePlayer(playerId);
      Common.toast('删除成功');
      await this.loadPlayers();
    } catch (e) { Common.toast('删除失败'); }
  },

  async viewDetail(playerId) {
    const el = document.getElementById('playerDetailContent');
    const p = this.players.find(x => x.playerId === playerId);
    document.getElementById('playerDetailTitle').textContent = p ? p.playerName : '球员详情';
    el.innerHTML = '<div class="loading">加载中...</div>';
    Common.openModal('playerDetailModal');
    let html = '';
    if (p) {
      const team = this.teams.find(t => t.teamId === p.teamId);
      html += `<div style="margin-bottom:14px">
        <strong>姓名:</strong> ${p.playerName} &nbsp;
        <strong>球队:</strong> ${team ? team.teamName : p.teamId || '-'} &nbsp;
        <strong>号码:</strong> ${p.number} &nbsp;
        <strong>位置:</strong> ${p.position || '-'}
      </div>`;
    }
    // 事件
    try {
      const events = await API.getPlayerEvents(playerId);
      const list = events || [];
      if (list.length) {
        html += '<h3 style="margin:14px 0 8px">事件记录</h3><table class="table"><thead><tr><th>时间</th><th>类型</th><th>描述</th></tr></thead><tbody>';
        list.forEach(e => html += `<tr><td>${Common.formatTime(e.eventTime)}</td><td>${Common.eventBadge(e.eventType)}</td><td>${e.description || '-'}</td></tr>`);
        html += '</tbody></table>';
      } else { html += '<p style="color:var(--text-muted)">暂无事件记录</p>'; }
    } catch (e) { html += '<p style="color:var(--text-muted)">事件数据加载失败</p>'; }
    // 统计
    try {
      const stats = await API.getPlayerStatistics(playerId);
      const sList = stats || [];
      if (sList.length) {
        html += '<h3 style="margin:14px 0 8px">统计数据</h3><table class="table"><thead><tr><th>统计项</th><th>数值</th></tr></thead><tbody>';
        sList.forEach(s => html += `<tr><td>${STAT_KEY[s.statKey] || s.statKey}</td><td>${s.statValue}</td></tr>`);
        html += '</tbody></table>';
      } else { html += '<p style="color:var(--text-muted)">暂无统计数据</p>'; }
    } catch (e) { html += '<p style="color:var(--text-muted)">统计数据加载失败</p>'; }
    el.innerHTML = html;
  }
};

document.addEventListener('DOMContentLoaded', () => PlayersPage.init());
