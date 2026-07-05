const API = (() => {
  const BASE = '';

  function toQuery(params) {
    const parts = [];
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') {
        parts.push(encodeURIComponent(k) + '=' + encodeURIComponent(v));
      }
    });
    return parts.length ? '?' + parts.join('&') : '';
  }

  function extractData(res) {
    if (res && typeof res === 'object' && 'success' in res) {
      if (!res.success) {
        throw new Error(res.message || '操作失败');
      }
      return res.data;
    }
    return res;
  }

  async function request(url, options = {}, responseType = 'json') {
    const headers = { 'Content-Type': 'application/json' };
    // 自动携带 token
    const token = localStorage.getItem('token');
    if (token) {
      headers['Authorization'] = 'Bearer ' + token;
    }
    const config = { headers, ...options }
    if (config.body && typeof config.body === 'string') {
      config.headers['Content-Type'] = 'application/json';
    }
    try {
      const resp = await fetch(BASE + url, config);
      // 401 未登录，跳转登录页
      if (resp.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        localStorage.removeItem('username');
        localStorage.removeItem('nickname');
        localStorage.removeItem('role');
        window.location.href = './login.html';
        throw new Error('登录已过期，请重新登录');
      }
      if (resp.status === 403) {
        throw new Error('权限不足');
      }
      if (!resp.ok) {
        const text = await resp.text();
        throw new Error(text || `请求失败: ${resp.status}`);
      }
      if (responseType === 'text') {
        return await resp.text();
      }
      const data = await resp.json();
      return extractData(data);
    } catch (error) {
      console.error('API 请求异常：', url, error);
      throw error;
    }
  }

  return {
    // Dashboard
    getDashboardSummary() { return request('/api/dashboard/summary'); },

    // Matches
    getMatches(params = {}) { return request(`/api/matches${toQuery(params)}`); },
    getMatch(id) { return request(`/api/matches/${id}`); },
    createMatch(data) { return request('/api/matches', { method: 'POST', body: JSON.stringify(data) }); },
    updateMatch(id, data) { return request(`/api/matches/${id}`, { method: 'PUT', body: JSON.stringify(data) }); },
    deleteMatch(id) { return request(`/api/matches/${id}`, { method: 'DELETE' }); },
    setTeams(matchId, data) { return request(`/api/matches/${matchId}/teams`, { method: 'POST', body: JSON.stringify(data) }); },
    addPlayerToMatch(matchId, data) { return request(`/api/matches/${matchId}/players`, { method: 'POST', body: JSON.stringify(data) }); },
    changeMatchStatus(matchId, action) { return request(`/api/matches/${matchId}/${action}`, { method: 'POST' }); },
    getStatistics(matchId) { return request(`/api/matches/${matchId}/statistics`); },
    getAnalysis(matchId) { return request(`/api/matches/${matchId}/analysis`, {}, 'text'); },
    getReviewReport(matchId) { return request(`/api/matches/${matchId}/report`, {}, 'text'); },

    // Events
    recordEvent(matchId, data) { return request(`/api/matches/${matchId}/events`, { method: 'POST', body: JSON.stringify(data) }); },
    getMatchEvents(matchId) { return request(`/api/matches/${matchId}/events`); },
    getFilteredEvents(params = {}) { return request(`/api/events${toQuery(params)}`); },
    getEvent(eventId) { return request(`/api/events/${eventId}`); },
    updateEvent(eventId, data) { return request(`/api/events/${eventId}`, { method: 'PUT', body: JSON.stringify(data) }); },
    deleteEvent(eventId) { return request(`/api/events/${eventId}`, { method: 'DELETE' }); },

    // Teams
    getTeams() { return request('/api/teams'); },
    getTeam(teamId) { return request(`/api/teams/${teamId}`); },
    createTeam(data) { return request('/api/teams', { method: 'POST', body: JSON.stringify(data) }); },
    updateTeam(teamId, data) { return request(`/api/teams/${teamId}`, { method: 'PUT', body: JSON.stringify(data) }); },
    deleteTeam(teamId) { return request(`/api/teams/${teamId}`, { method: 'DELETE' }); },
    getTeamPlayers(teamId) { return request(`/api/teams/${teamId}/players`); },
    getTeamMatches(teamId) { return request(`/api/teams/${teamId}/matches`); },

    // Players
    getPlayers(params = {}) { return request(`/api/players${toQuery(params)}`); },
    getPlayer(playerId) { return request(`/api/players/${playerId}`); },
    createPlayer(data) { return request('/api/players', { method: 'POST', body: JSON.stringify(data) }); },
    updatePlayer(playerId, data) { return request(`/api/players/${playerId}`, { method: 'PUT', body: JSON.stringify(data) }); },
    deletePlayer(playerId) { return request(`/api/players/${playerId}`, { method: 'DELETE' }); },
    getPlayerEvents(playerId) { return request(`/api/players/${playerId}/events`); },
    getPlayerStatistics(playerId) { return request(`/api/players/${playerId}/statistics`); },

    // Rankings
    getPlayerRankings(params = {}) { return request(`/api/rankings/players${toQuery(params)}`); },
    getOverallPlayerRankings(params = {}) { return request(`/api/rankings/players/overall${toQuery(params)}`); },


    // Reports
    getMatchReports(matchId) { return request(`/api/matches/${matchId}/reports`); },
    getReportDetail(reportId) { return request(`/api/reports/${reportId}`); },
    saveReport(matchId, data) { return request(`/api/matches/${matchId}/reports`, { method: 'POST', body: JSON.stringify(data) }); },
    generateAndSaveReport(matchId, reportType) { return request(`/api/matches/${matchId}/reports/generate?reportType=${reportType}`, { method: 'POST' }); },
    deleteReport(reportId) { return request(`/api/reports/${reportId}`, { method: 'DELETE' }); },

    // SSE stream（EventSource 不支持自定义请求头，通过 query 参数传递 token）
    streamAnalysis(matchId) {
      const token = localStorage.getItem('token') || '';
      return BASE + `/api/matches/${matchId}/analysis/stream?token=${encodeURIComponent(token)}`;
    },
    streamReport(matchId) {
      const token = localStorage.getItem('token') || '';
      return BASE + `/api/matches/${matchId}/report/stream?token=${encodeURIComponent(token)}`;
    },

    // Auth / Profile
    getMe() { return request('/api/auth/me'); },
    updateNickname(nickname) { return request('/api/auth/nickname', { method: 'PUT', body: JSON.stringify({ nickname }) }); },
    updatePassword(oldPassword, newPassword) { return request('/api/auth/password', { method: 'PUT', body: JSON.stringify({ oldPassword, newPassword }) }); },

    // Client (USER)
    clientQueryMatches(params = {}) { return request(`/api/client/matches${toQuery(params)}`); },
    clientRecommendMatches(preference) { return request('/api/client/matches/recommend', { method: 'POST', body: JSON.stringify({ preference }) }); },
    clientGetMatch(matchId) { return request(`/api/client/matches/${matchId}`); },
    clientGetMatchEvents(matchId) { return request(`/api/client/matches/${matchId}/events`); },
    clientGetStatistics(matchId) { return request(`/api/client/matches/${matchId}/statistics`); },
    clientGetMatchReports(matchId) { return request(`/api/client/matches/${matchId}/reports`); },

    // SSE 赛事问答助手
    streamQa(matchId, question) {
      const token = localStorage.getItem('token') || '';
      return BASE + `/api/client/matches/${matchId}/qa/stream?token=${encodeURIComponent(token)}&question=${encodeURIComponent(question)}`;
    },
    getQaHistory(matchId) { return request(`/api/client/matches/${matchId}/qa/history`); },
    clearQaHistory(matchId) { return request(`/api/client/matches/${matchId}/qa/history`, { method: 'DELETE' }); },

    // Client Teams (USER)
    clientGetTeams() { return request('/api/client/teams'); },
    clientGetTeam(teamId) { return request(`/api/client/teams/${teamId}`); }
  };
})();
