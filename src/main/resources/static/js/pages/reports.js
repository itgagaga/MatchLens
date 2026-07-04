const ReportsPage = {
  matches: [],

  async init() {
    try {
      this.matches = await API.getMatches() || [];
    } catch (e) { this.matches = []; }
    const sel = document.getElementById('reportMatchId');
    sel.innerHTML = '<option value="">请选择比赛</option>' +
      this.matches.map(m => `<option value="${m.matchId}">${m.matchName || '未命名比赛'}</option>`).join('');

    // URL 参数 matchId
    const matchId = Common.getParam('matchId');
    if (matchId) {
      sel.value = matchId;
      await this.loadReports();
    }
  },

  async loadReports() {
    const listEl = document.getElementById('reportList');
    const matchId = document.getElementById('reportMatchId').value;
    if (!matchId) { Common.showEmpty(listEl, '请选择比赛'); return; }
    Common.showLoading(listEl);
    try {
      const data = await API.getMatchReports(matchId);
      const reports = data || [];
      if (!reports.length) { Common.showEmpty(listEl, '暂无报告'); return; }
      this.renderReports(reports);
    } catch (e) {
      Common.showError(listEl, '该功能需要后端接口支持');
    }
  },

  renderReports(reports) {
    const listEl = document.getElementById('reportList');
    listEl.innerHTML = reports.map(r => {
      const typeBadge = r.reportType === 'SITUATION'
        ? '<span class="badge badge-orange">态势分析</span>'
        : r.reportType === 'REVIEW'
          ? '<span class="badge badge-red">赛后复盘</span>'
          : `<span class="badge badge-blue">${r.reportType || '未知'}</span>`;
      return `
        <div class="report-card">
          <div style="display:flex;justify-content:space-between;align-items:center">
            <div>
              <span class="report-title">${r.title || '未命名报告'}</span>
              ${typeBadge}
            </div>
            <div class="action-group">
              <button class="btn btn-sm" onclick="ReportsPage.viewDetail('${r.reportId}')">查看</button>
              <button class="btn btn-sm btn-red" onclick="ReportsPage.deleteReport('${r.reportId}')">删除</button>
            </div>
          </div>
          <div class="report-meta">
            生成方式: ${r.generatedBy || '-'} · 创建时间: ${Common.formatTime(r.createTime)}
          </div>
        </div>
      `;
    }).join('');
  },

  generateReport(type) {
    const matchId = document.getElementById('reportMatchId').value;
    if (!matchId) { Common.toast('请选择比赛'); return; }

    // 禁用生成按钮，防止重复点击
    const btns = document.querySelectorAll('.btn-orange, .btn-red');
    btns.forEach(b => b.disabled = true);

    // 显示流式预览区域
    const streamSection = document.getElementById('streamSection');
    const streamPreview = document.getElementById('streamPreview');
    const streamTitle = document.getElementById('streamTitle');
    const streamBadge = document.getElementById('streamBadge');
    streamSection.style.display = '';
    streamPreview.innerHTML = '';
    streamTitle.textContent = type === 'SITUATION' ? '正在生成态势报告...' : '正在生成复盘报告...';
    streamBadge.textContent = '● 生成中';
    streamBadge.className = 'streaming-badge';

    // 选择对应的 SSE 端点
    const sseUrl = type === 'SITUATION'
      ? API.streamAnalysis(matchId)
      : API.streamReport(matchId);

    let fullContent = '';
    let completed = false;
    const es = new EventSource(sseUrl);

    es.addEventListener('start', () => {
      streamPreview.innerHTML = '<div class="stream-cursor"></div>';
    });

    es.addEventListener('chunk', (e) => {
      fullContent += e.data;
      streamPreview.innerHTML = Common.markdownToHtml(fullContent) + '<div class="stream-cursor"></div>';
      // 自动滚动到底部
      streamPreview.scrollTop = streamPreview.scrollHeight;
    });

    es.addEventListener('done', () => {
      completed = true;
      es.close();
      streamTitle.textContent = '报告生成完成';
      streamBadge.textContent = '✓ 已完成';
      streamBadge.className = 'streaming-badge done';
      // 移除光标
      const cursor = streamPreview.querySelector('.stream-cursor');
      if (cursor) cursor.remove();
      // 恢复按钮
      btns.forEach(b => b.disabled = false);
      Common.toast('报告生成成功');
      // 刷新报告列表
      this.loadReports();
    });

    es.addEventListener('error', (e) => {
      completed = true;
      es.close();
      streamTitle.textContent = '生成失败';
      streamBadge.textContent = '✗ 失败';
      streamBadge.className = 'streaming-badge error';
      btns.forEach(b => b.disabled = false);
      if (e.data) {
        streamPreview.innerHTML += `<div style="color:var(--danger);margin-top:8px">${e.data}</div>`;
      }
      Common.toast('生成失败');
    });

    es.onerror = () => {
      // 正常完成后服务端关闭连接也会触发 onerror，忽略即可
      if (completed) { es.close(); return; }
      es.close();
      streamTitle.textContent = '连接中断';
      streamBadge.textContent = '✗ 中断';
      streamBadge.className = 'streaming-badge error';
      btns.forEach(b => b.disabled = false);
    };
  },

  async viewDetail(reportId) {
    const el = document.getElementById('reportDetailContent');
    el.innerHTML = '<div class="loading">加载中...</div>';
    Common.openModal('reportDetailModal');
    try {
      const report = await API.getReportDetail(reportId);
      if (!report) { el.innerHTML = '<div class="empty">报告不存在</div>'; return; }
      document.getElementById('reportDetailTitle').textContent = report.title || '报告详情';
      let html = `
        <div style="margin-bottom:12px">
          <strong>类型:</strong> ${report.reportType || '-'} &nbsp;
          <strong>生成方式:</strong> ${report.generatedBy || '-'} &nbsp;
          <strong>时间:</strong> ${Common.formatTime(report.createTime)}
        </div>
      `;
      if (report.content) {
        html += '<h3 style="margin:14px 0 8px">报告内容 (Markdown 预览)</h3>';
        html += `<div class="md-preview">${Common.markdownToHtml(report.content)}</div>`;
        html += '<h3 style="margin:14px 0 8px">原始 Markdown</h3>';
        html += `<div class="log-code">${report.content}</div>`;
      }
      el.innerHTML = html;
    } catch (e) { el.innerHTML = '<div class="empty">加载失败</div>'; }
  },

  async deleteReport(reportId) {
    if (!confirm('确认删除该报告？')) return;
    try {
      await API.deleteReport(reportId);
      Common.toast('删除成功');
      await this.loadReports();
    } catch (e) { Common.toast('删除失败'); }
  }
};

document.addEventListener('DOMContentLoaded', () => ReportsPage.init());
