const ClientPage = {
  allMatches: [],

  init() {
    // 检查 USER 登录状态
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    if (!token || role === 'ADMIN') {
      window.location.href = './login.html';
      return;
    }
    const nickname = localStorage.getItem('nickname') || localStorage.getItem('username');
    document.getElementById('userGreeting').textContent = '👋 ' + nickname;
    document.getElementById('welcomeTitle').textContent = '欢迎回来，' + nickname + '！';

    // 回车键触发搜索
    document.getElementById('filterKeyword').addEventListener('keyup', (e) => {
      if (e.key === 'Enter') this.loadMatches();
    });
    // 回车键触发 AI 推荐
    document.getElementById('aiPreference').addEventListener('keyup', (e) => {
      if (e.key === 'Enter') this.recommendMatches();
    });

    this.loadMatches();
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('nickname');
    localStorage.removeItem('role');
    window.location.href = './login.html';
  },

  toast(msg, duration = 2500) {
    const el = document.getElementById('toast');
    el.textContent = msg;
    el.classList.add('show');
    clearTimeout(el._timer);
    el._timer = setTimeout(() => el.classList.remove('show'), duration);
  },

  // ========== 条件查询 ==========
  async loadMatches() {
    const listEl = document.getElementById('matchList');
    listEl.innerHTML = '<div class="loading">加载中...</div>';

    try {
      const params = {
        sportType: document.getElementById('filterSportType').value,
        status: document.getElementById('filterStatus').value,
        keyword: document.getElementById('filterKeyword').value
      };
      this.allMatches = await API.clientQueryMatches(params) || [];
      this.renderMatches(this.allMatches);
    } catch (e) {
      listEl.innerHTML = '<div class="empty" style="color:var(--danger)">加载比赛列表失败</div>';
    }
  },

  renderMatches(matches) {
    const listEl = document.getElementById('matchList');

    if (!matches || !matches.length) {
      listEl.innerHTML = '<div class="empty">暂无比赛</div>';
      return;
    }

    listEl.innerHTML = matches.map(m => {
      const home = m.teamA ? m.teamA.teamName : '甲方';
      const away = m.teamB ? m.teamB.teamName : '乙方';
      const homeScore = m.statistics ? m.statistics.scoreA : 0;
      const awayScore = m.statistics ? m.statistics.scoreB : 0;

      return `
        <div class="match-card">
          <div style="display:flex;justify-content:space-between;align-items:center">
            <div class="match-title">${this.escapeHtml(m.matchName || '未命名比赛')}</div>
            ${this.statusBadge(m.status)}
          </div>
          <div style="margin:12px 0">
            <div class="match-vs">${this.escapeHtml(home)} vs ${this.escapeHtml(away)}</div>
            <div class="match-score" style="margin-top:8px">${homeScore} : ${awayScore}</div>
          </div>
          <div class="match-meta">
            ${this.sportBadge(m.sportType)} · ${this.formatTime(m.createTime)}
          </div>
          <div style="margin-top:12px">
            <a href="./client-match-detail.html?matchId=${m.matchId}" class="btn btn-sm">进入详情</a>
          </div>
        </div>
      `;
    }).join('');
  },

  // ========== AI 智能推荐 ==========
  async recommendMatches() {
    const preference = document.getElementById('aiPreference').value.trim();
    if (!preference) {
      this.toast('请输入您想看的比赛描述');
      return;
    }

    const resultEl = document.getElementById('aiRecommendResult');
    const btn = document.getElementById('btnAiRecommend');

    // 显示加载状态
    btn.disabled = true;
    btn.textContent = '⏳ AI 分析中...';
    resultEl.style.display = 'block';
    resultEl.innerHTML = '<div class="loading">AI 正在分析比赛数据，请稍候...</div>';

    try {
      const data = await API.clientRecommendMatches(preference);

      // 判断返回格式
      if (data.rawResponse) {
        // AI 返回格式异常，显示原始文本
        resultEl.innerHTML = `
          <div style="color: var(--warning); margin-bottom: 10px; font-size: 13px;">
            ⚠️ ${this.escapeHtml(data.message || 'AI 返回格式异常')}
          </div>
          <div class="ai-result">${this.escapeHtml(data.rawResponse)}</div>
        `;
      } else if (Array.isArray(data) && data.length > 0) {
        // 正常推荐结果
        this.renderRecommendations(data, resultEl);
      } else {
        resultEl.innerHTML = '<div class="empty">未找到匹配的比赛，请尝试调整描述</div>';
      }
    } catch (e) {
      resultEl.innerHTML = `<div class="empty" style="color:var(--danger)">推荐失败：${this.escapeHtml(e.message)}</div>`;
    } finally {
      btn.disabled = false;
      btn.textContent = '✨ 智能推荐';
    }
  },

  renderRecommendations(recommendations, container) {
    const html = recommendations.map((rec, index) => {
      const home = rec.teamA ? rec.teamA.teamName : '甲方';
      const away = rec.teamB ? rec.teamB.teamName : '乙方';
      const scoreA = rec.statistics ? rec.statistics.scoreA : 0;
      const scoreB = rec.statistics ? rec.statistics.scoreB : 0;
      const scoreDiff = rec.statistics ? rec.statistics.scoreDifference : 0;

      const medalIcon = index === 0 ? '🥇' : index === 1 ? '🥈' : index === 2 ? '🥉' : `#${index + 1}`;
      const matchScore = typeof rec.recommendScore === 'number' ? rec.recommendScore : (rec.recommendScore || 0);

      return `
        <div class="match-card" style="border-left: 4px solid ${index === 0 ? '#fbbf24' : index === 1 ? '#94a3b8' : index === 2 ? '#d97706' : 'var(--border)'}; margin-bottom: 12px;">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px">
            <div style="font-size:18px;font-weight:700">
              ${medalIcon} ${this.escapeHtml(rec.matchName || '未命名比赛')}
            </div>
            <div style="display:flex;gap:6px;align-items:center">
              <span class="badge badge-green">匹配度 ${matchScore}%</span>
              ${this.statusBadge(rec.status)}
            </div>
          </div>
          <div style="margin:8px 0">
            <div class="match-vs">${this.escapeHtml(home)} vs ${this.escapeHtml(away)}</div>
            <div class="match-score" style="margin-top:6px">${scoreA} : ${scoreB}</div>
          </div>
          <div style="margin-top:10px;padding:10px 14px;background:rgba(79,70,229,0.05);border-radius:12px;font-size:14px;color:#475569">
            💡 ${this.escapeHtml(rec.reason || '暂无推荐理由')}
          </div>
          <div style="margin-top:10px;display:flex;gap:8px;align-items:center">
            ${this.sportBadge(rec.sportType)}
            ${scoreDiff <= 5 && scoreA + scoreB > 0 ? '<span class="badge badge-orange" style="margin-left:4px">焦灼</span>' : ''}
          </div>
          <div style="margin-top:10px">
            <a href="./client-match-detail.html?matchId=${rec.matchId}" class="btn btn-sm">进入详情</a>
          </div>
        </div>
      `;
    }).join('');

    container.innerHTML = `
      <div style="margin-bottom:12px;font-weight:700;font-size:15px;color:var(--primary)">
        🎯 为您推荐 ${recommendations.length} 场比赛
      </div>
      ${html}
    `;
  },

  // ========== 工具方法 ==========
  statusBadge(status) {
    const s = MATCH_STATUS[status] || { label: status, badge: 'badge-gray' };
    return `<span class="badge ${s.badge}">${s.label}</span>`;
  },

  sportBadge(sportType) {
    const s = SPORT_TYPE[sportType] || { label: sportType, icon: '🏅' };
    return `${s.icon} ${s.label}`;
  },

  formatTime(dt) {
    if (!dt) return '-';
    if (Array.isArray(dt)) {
      const [y, m, d, h = 0, min = 0] = dt;
      return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}`;
    }
    if (typeof dt === 'string') return dt.replace('T', ' ').substring(0, 16);
    return new Date(dt).toLocaleString('zh-CN');
  },

  escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  }
};

document.addEventListener('DOMContentLoaded', () => ClientPage.init());
