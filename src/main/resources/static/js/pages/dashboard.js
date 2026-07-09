(async function () {
  const summaryEl = document.getElementById('summaryCards');
  const recentEl = document.getElementById('recentMatches');
  const aiEl = document.getElementById('aiOverview');

  // 加载看板
  async function loadSummary() {
    const data = await Common.safeCall(summaryEl, () => API.getDashboardSummary());
    if (!data) {
      summaryEl.innerHTML = '';
      const cards = [
        { label: '比赛总数', num: '-' },
        { label: '球队总数', num: '-' },
        { label: '球员总数', num: '-' },
        { label: '事件总数', num: '-' }
      ];
      summaryEl.innerHTML = cards.map(c =>
        `<div class="summary-card"><div class="num">${c.num}</div><div class="label">${c.label}</div></div>`
      ).join('');
      return;
    }
    const cards = [
      { label: '比赛总数', num: data.matchCount ?? 0, color: 'var(--primary)' },
      { label: '球队总数', num: data.teamCount ?? 0, color: 'var(--accent)' },
      { label: '球员总数', num: data.playerCount ?? 0, color: 'var(--success)' },
      { label: '事件总数', num: data.eventCount ?? 0, color: 'var(--secondary)' }
    ];
    summaryEl.innerHTML = cards.map(c =>
      `<div class="summary-card"><div class="num">${c.num}</div><div class="label">${c.label}</div></div>`
    ).join('');

    // AI 概览
    aiEl.innerHTML = `
      <div class="grid grid-4" style="margin-top:12px">
        <div class="summary-card"><div class="num">${data.aiCallCount ?? 0}</div><div class="label">AI 调用总数</div></div>
        <div class="summary-card"><div class="num">${data.aiSuccessCount ?? 0}</div><div class="label">成功次数</div></div>
        <div class="summary-card"><div class="num">${data.aiFailCount ?? 0}</div><div class="label">失败次数</div></div>
        <div class="summary-card"><div class="num">${data.aiSuccessRate != null ? data.aiSuccessRate.toFixed(1) + '%' : '-'}</div><div class="label">成功率</div></div>
      </div>
    `;
  }

  // 加载最近比赛
  async function loadRecentMatches() {
    Common.showLoading(recentEl);
    const matches = await Common.safeCall(recentEl, () => API.getMatches());
    if (!matches || !matches.length) {
      Common.showEmpty(recentEl, '暂无比赛');
      return;
    }
    const list = matches.slice(0, 6);
    recentEl.innerHTML = list.map(m => {
      const home = m.teamA ? m.teamA.teamName : '甲方';
      const away = m.teamB ? m.teamB.teamName : '乙方';
      const homeScore = m.statistics ? m.statistics.scoreA : 0;
      const awayScore = m.statistics ? m.statistics.scoreB : 0;
      return `
        <div class="recent-match-card">
          <div>
            <span style="font-weight:700">${home}</span>
            <span class="vs"> vs </span>
            <span style="font-weight:700">${away}</span>
          </div>
          <div>
            <span class="match-score">${homeScore} : ${awayScore}</span>
            ${Common.statusBadge(m.status)}
            <a href="./match-detail.html?matchId=${m.matchId}" class="btn btn-sm" style="margin-left:10px">查看详情</a>
          </div>
        </div>
      `;
    }).join('');
  }

  await loadSummary();
  await loadRecentMatches();
})();
