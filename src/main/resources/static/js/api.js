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
  },

  async streamRequest(url, onChunk, onDone, onError) {
    try {
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`请求失败：${response.status}`);
      }
      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';
      let eventType = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });

        const lines = buffer.split('\n');
        buffer = lines.pop();

        for (const line of lines) {
          const trimmed = line.trim();
          if (!trimmed) {
            eventType = '';
            continue;
          }
          if (trimmed.startsWith('event:')) {
            eventType = trimmed.substring(6).trim();
          } else if (trimmed.startsWith('data:')) {
            const data = trimmed.substring(5).trim();
            if (eventType === 'chunk' && data) {
              onChunk(data);
            } else if (eventType === 'done') {
              onDone();
              return;
            } else if (eventType === 'error') {
              onError(data);
              return;
            }
            eventType = '';
          }
        }
      }
      onDone();
    } catch (e) {
      onError(e.message);
    }
  },

  streamAnalysis(matchId, onChunk, onDone, onError) {
    return this.streamRequest(`/api/matches/${matchId}/analysis/stream`, onChunk, onDone, onError);
  },

  streamReport(matchId, onChunk, onDone, onError) {
    return this.streamRequest(`/api/matches/${matchId}/report/stream`, onChunk, onDone, onError);
  },

  getMatchEvents(matchId) {
    return this.request(`/api/matches/${matchId}/events`);
  },

  getEvent(eventId) {
    return this.request(`/api/events/${eventId}`);
  },

  updateEvent(eventId, data) {
    return this.request(`/api/events/${eventId}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  deleteEvent(eventId) {
    return this.request(`/api/events/${eventId}`, {
      method: 'DELETE'
    });
  },

  getEventsFiltered(params) {
    const query = Object.entries(params)
      .filter(([, v]) => v != null && v !== '')
      .map(([k, v]) => `${k}=${encodeURIComponent(v)}`)
      .join('&');
    return this.request(`/api/events${query ? '?' + query : ''}`);
  },

  getTeams() {
    return this.request('/api/teams');
  },

  getPlayers(teamId) {
    const url = teamId ? `/api/players?teamId=${teamId}` : '/api/players';
    return this.request(url);
  },

  getPlayer(playerId) {
    return this.request(`/api/players/${playerId}`);
  },

  createPlayer(data) {
    return this.request('/api/players', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  updatePlayer(playerId, data) {
    return this.request(`/api/players/${playerId}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  deletePlayer(playerId) {
    return this.request(`/api/players/${playerId}`, {
      method: 'DELETE'
    });
  },

  getPlayerEvents(playerId) {
    return this.request(`/api/players/${playerId}/events`);
  },

  getPlayerStatistics(playerId) {
    return this.request(`/api/players/${playerId}/statistics`);
  }
};
