const Common = {
  // 获取 URL 参数
  getParam(name) {
    const url = new URL(window.location.href);
    return url.searchParams.get(name);
  },

  // Toast 提示
  toast(msg, duration = 2500) {
    const el = document.getElementById('toast');
    if (!el) return;
    el.textContent = msg;
    el.classList.add('show');
    clearTimeout(el._timer);
    el._timer = setTimeout(() => el.classList.remove('show'), duration);
  },

  // 格式化时间
  formatTime(dt) {
    if (!dt) return '-';
    if (Array.isArray(dt)) {
      const [y, m, d, h = 0, min = 0, s = 0] = dt;
      return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
    }
    if (typeof dt === 'string') return dt.replace('T', ' ').substring(0, 19);
    return new Date(dt).toLocaleString('zh-CN');
  },

  // 获取比赛状态标签
  statusBadge(status) {
    const s = MATCH_STATUS[status] || { label: status, badge: 'badge-gray' };
    return `<span class="badge ${s.badge}">${s.label}</span>`;
  },

  // 获取赛事类型标签
  sportBadge(sportType) {
    const s = SPORT_TYPE[sportType] || { label: sportType, icon: '🏅' };
    return `${s.icon} ${s.label}`;
  },

  // 统计项名称映射
  eventTypeName(key) {
    if (typeof STAT_KEY !== 'undefined' && STAT_KEY[key]) return STAT_KEY[key];
    const e = EVENT_TYPE[key];
    if (e) return e.label;
    return key;
  },

  // 获取事件类型标签
  eventBadge(eventType) {
    const e = EVENT_TYPE[eventType] || { label: eventType, badge: 'badge-gray' };
    return `<span class="badge ${e.badge}">${e.label}</span>`;
  },

  // 导航栏高亮
  highlightNav() {
    const page = window.location.pathname.split('/').pop() || 'index.html';
    document.querySelectorAll('.nav-link').forEach(link => {
      const href = link.getAttribute('href');
      if (href === './' + page || href === page) {
        link.classList.add('active');
      }
    });
  },

  // 显示加载
  showLoading(container) {
    container.innerHTML = '<div class="loading">加载中...</div>';
  },

  // 显示空状态
  showEmpty(container, msg = '暂无数据') {
    container.innerHTML = `<div class="empty">${msg}</div>`;
  },

  // 显示错误
  showError(container, msg) {
    container.innerHTML = `<div class="empty" style="color:var(--danger)">${msg}</div>`;
  },

  // 安全调用 API，失败时显示错误
  async safeCall(container, fn, errorMsg = '该功能需要后端接口支持') {
    try {
      return await fn();
    } catch (e) {
      console.error(e);
      if (container) {
        Common.showError(container, errorMsg);
      }
      return null;
    }
  },

  // 简易 Markdown 转 HTML
  markdownToHtml(md) {
    if (!md) return '';
    let html = md
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/^### (.+)$/gm, '<h3>$1</h3>')
      .replace(/^## (.+)$/gm, '<h2>$1</h2>')
      .replace(/^# (.+)$/gm, '<h1>$1</h1>')
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.+?)\*/g, '<em>$1</em>')
      .replace(/^- (.+)$/gm, '<li>$1</li>')
      .replace(/(<li>.*<\/li>)/gs, '<ul>$1</ul>')
      .replace(/<\/ul>\s*<ul>/g, '')
      .replace(/\n{2,}/g, '</p><p>')
      .replace(/\n/g, '<br>');
    return '<p>' + html + '</p>';
  },

  // 打开弹窗
  openModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.add('active');
  },

  // 关闭弹窗
  closeModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('active');
  },

  // 初始化页面
  init() {
    this.highlightNav();
  }
};

document.addEventListener('DOMContentLoaded', () => Common.init());
