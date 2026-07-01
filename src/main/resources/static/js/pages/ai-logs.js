const AiLogsPage = {
  matches: [],

  async init() {
    // 加载比赛列表
    try {
      this.matches = await API.getMatches() || [];
    } catch (e) { this.matches = []; }
    const sel = document.getElementById('filterMatchId');
    sel.innerHTML = '<option value="">全部比赛</option>' +
      this.matches.map(m => `<option value="${m.matchId}">${m.matchName || '未命名比赛'}</option>`).join('');

    // URL 参数 matchId
    const matchId = Common.getParam('matchId');
    if (matchId) {
      sel.value = matchId;
    }
    await this.load();
  },

  async load() {
    const listEl = document.getElementById('logList');
    Common.showLoading(listEl);
    const params = {};
    const matchId = document.getElementById('filterMatchId').value;
    const agentType = document.getElementById('filterAgentType').value;
    const success = document.getElementById('filterSuccess').value;
    const keyword = document.getElementById('filterKeyword').value.trim();

    if (matchId) params.matchId = matchId;
    if (agentType) params.agentType = agentType;
    if (success !== '') params.success = success;
    if (keyword) params.keyword = keyword;

    try {
      const data = await API.getAiLogs(params);
      const logs = data || [];
      if (!logs.length) { Common.showEmpty(listEl, '暂无 AI 调用记录'); return; }
      this.renderLogs(logs);
    } catch (e) {
      Common.showError(listEl, '该功能需要后端接口支持');
    }
  },

  renderLogs(logs) {
    const listEl = document.getElementById('logList');
    listEl.innerHTML = logs.map(log => {
      const successBadge = log.success
        ? '<span class="badge badge-green">成功</span>'
        : '<span class="badge badge-red">失败</span>';
      return `
        <div class="log-card">
          <div class="log-header">
            <div>
              ${successBadge}
              <span class="badge badge-blue" style="margin-left:6px">${log.agentType || '-'}</span>
              <span style="margin-left:8px;color:var(--text-muted);font-size:13px">${Common.formatTime(log.createTime)}</span>
            </div>
            <div class="action-group">
              <button class="btn btn-sm" onclick="AiLogsPage.viewDetail('${log.id || log.logId}')">详情</button>
              <button class="btn btn-sm btn-red" onclick="AiLogsPage.deleteLog('${log.id || log.logId}')">删除</button>
            </div>
          </div>
          <div style="font-size:14px;color:var(--text-muted);margin-top:4px">
            比赛: ${(this.matches.find(m => String(m.matchId) === String(log.matchId)) || {}).matchName || '未命名比赛'} · 耗时: ${log.duration ? log.duration + 'ms' : '-'}
          </div>
          ${log.errorMessage ? `<div style="color:var(--danger);margin-top:6px;font-size:13px">错误: ${log.errorMessage}</div>` : ''}
        </div>
      `;
    }).join('');
  },

  async viewDetail(id) {
    const el = document.getElementById('logDetailContent');
    el.innerHTML = '<div class="loading">加载中...</div>';
    Common.openModal('logDetailModal');
    try {
      const log = await API.getAiLog(id);
      if (!log) { el.innerHTML = '<div class="empty">日志不存在</div>'; return; }
      let html = `
        <div style="margin-bottom:12px">
          <strong>Agent:</strong> ${log.agentType || '-'} &nbsp;
          <strong>成功:</strong> ${log.success ? '是' : '否'} &nbsp;
          <strong>耗时:</strong> ${log.duration ? log.duration + 'ms' : '-'}
        </div>
      `;
      if (log.prompt) {
        html += `<h3 style="margin:12px 0 6px">Prompt</h3><div class="log-code">${log.prompt}</div>`;
      }
      if (log.response) {
        html += `<h3 style="margin:12px 0 6px">Response</h3><div class="log-code">${log.response}</div>`;
      }
      if (log.errorMessage) {
        html += `<h3 style="margin:12px 0 6px;color:var(--danger)">错误信息</h3><div class="log-code" style="border:1px solid var(--danger)">${log.errorMessage}</div>`;
      }
      el.innerHTML = html;
    } catch (e) { el.innerHTML = '<div class="empty">加载失败</div>'; }
  },

  async deleteLog(id) {
    if (!confirm('确认删除该日志？')) return;
    try {
      await API.deleteAiLog(id);
      Common.toast('删除成功');
      await this.load();
    } catch (e) { Common.toast('删除失败'); }
  }
};

document.addEventListener('DOMContentLoaded', () => AiLogsPage.init());
