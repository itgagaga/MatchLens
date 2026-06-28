const api = {
  async request(url, options = {}, responseType = 'json') {
    const config = {
      headers: { 'Content-Type': 'application/json' },
      ...options
    };

    const response = await fetch(url, config);

    if (!response.ok) {
      throw new Error(`请求失败：${response.status}`);
    }

    if (responseType === 'text') {
      return response.text();
    }

    return response.json();
  },

  getMatches() {
    return this.request('/api/matches');
  },

  getMatch(id) {
    return this.request(`/api/matches/${id}`);
  },

  createMatch(data) {
    return this.request('/api/matches', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  setTeams(matchId, data) {
    return this.request(`/api/matches/${matchId}/teams`, {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  addPlayer(matchId, data) {
    return this.request(`/api/matches/${matchId}/players`, {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  changeStatus(matchId, action) {
    return this.request(`/api/matches/${matchId}/${action}`, {
      method: 'POST'
    });
  },

  recordEvent(matchId, data) {
    return this.request(`/api/matches/${matchId}/events`, {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  getStatistics(matchId) {
    return this.request(`/api/matches/${matchId}/statistics`);
  },

  getAnalysis(matchId) {
    return this.request(`/api/matches/${matchId}/analysis`, {}, 'text');
  },

  getReport(matchId) {
    return this.request(`/api/matches/${matchId}/report`, {}, 'text');
  }
};
