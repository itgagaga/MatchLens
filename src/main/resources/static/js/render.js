function renderAll() {
  renderMatchList();
  renderOverview();
  renderTeamConfig();
  renderStatusActions();
  renderEventForm();
  renderEventTimeline();
  renderStatistics();
}

function renderMatchList() {
  const container = document.querySelector('#matchList');

  if (!state.matches.length) {
    container.innerHTML = '<p class="empty">暂无比赛，请先创建。</p>';
    return;
  }

  container.innerHTML = state.matches.map(match => `
    <div class="match-item ${state.currentMatch?.matchId === match.matchId ? 'active' : ''}"
         onclick="selectMatch('${match.matchId}')">
      <span class="match-item-name">${match.matchName}</span>
      <div class="match-item-meta">
        <span>${SPORT_TYPES.find(s => s.value === match.sportType)?.label || match.sportType}</span>
        <span>${STATUS_TEXT[match.status] || match.status}</span>
      </div>
    </div>
  `).join('');
}

function renderOverview() {
  const match = state.currentMatch;
  const container = document.querySelector('#matchOverview');

  if (!match) {
    container.innerHTML = '<p class="empty">请从左侧选择一场比赛。</p>';
    return;
  }

  const home = match.homeTeam;
  const away = match.awayTeam;

  if (!home || !away) {
    container.innerHTML = `
      <p><strong>比赛名称：</strong>${match.matchName}</p>
      <p><strong>赛事类型：</strong>${SPORT_TYPES.find(s => s.value === match.sportType)?.label || match.sportType}</p>
      <p><strong>比赛状态：</strong><span class="status-badge status-${match.status}">${STATUS_TEXT[match.status]}</span></p>
      <p class="empty">请先设置主队和客队。</p>
    `;
    return;
  }

  const leading = home.score > away.score ? home.teamName : away.score > home.score ? away.teamName : '平局';
  const diff = Math.abs(home.score - away.score);

  container.innerHTML = `
    <div class="scoreboard">
      <div class="team-name">${home.teamName}</div>
      <div class="score">${home.score} : ${away.score}</div>
      <div class="team-name">${away.teamName}</div>
    </div>
    <div class="overview-info">
      <div class="info-item"><strong>赛事类型：</strong>${SPORT_TYPES.find(s => s.value === match.sportType)?.label || match.sportType}</div>
      <div class="info-item"><strong>比赛状态：</strong><span class="status-badge status-${match.status}">${STATUS_TEXT[match.status]}</span></div>
      <div class="info-item"><strong>事件总数：</strong>${(match.events || []).length}</div>
      <div class="info-item"><strong>领先方：</strong>${leading}</div>
      <div class="info-item"><strong>分差：</strong>${diff}</div>
    </div>
  `;
}

function renderTeamConfig() {
  const match = state.currentMatch;
  const setTeamsForm = document.querySelector('#setTeamsForm');
  const playerSection = document.querySelector('#playerSection');

  if (!match) {
    setTeamsForm.innerHTML = '<p class="empty">请先选择比赛。</p>';
    playerSection.innerHTML = '';
    return;
  }

  setTeamsForm.innerHTML = `
    <div class="form-group">
      <label>主队名称</label>
      <input id="homeName" placeholder="例如：湖人" value="${match.homeTeam?.teamName || ''}" />
    </div>
    <div class="form-group">
      <label>客队名称</label>
      <input id="awayName" placeholder="例如：勇士" value="${match.awayTeam?.teamName || ''}" />
    </div>
    <button type="button" class="btn-block" onclick="handleSetTeams()">设置队伍</button>
  `;

  if (!match.homeTeam || !match.awayTeam) {
    playerSection.innerHTML = '<p class="empty">请先设置主客队后再添加球员。</p>';
    return;
  }

  const homePlayers = match.homeTeam.players || [];
  const awayPlayers = match.awayTeam.players || [];

  playerSection.innerHTML = `
    <div style="margin-top: 16px;">
      <h3 style="font-size: 15px; margin-bottom: 10px;">添加球员</h3>
      <div style="display: flex; gap: 8px; flex-wrap: wrap; align-items: flex-end;">
        <div style="flex: 1; min-width: 80px;">
          <label style="font-size: 12px; color: #6b7280;">队伍</label>
          <select id="playerTeam">
            <option value="home">主队</option>
            <option value="away">客队</option>
          </select>
        </div>
        <div style="flex: 2; min-width: 100px;">
          <label style="font-size: 12px; color: #6b7280;">球员姓名</label>
          <input id="playerName" placeholder="姓名" />
        </div>
        <div style="flex: 1; min-width: 70px;">
          <label style="font-size: 12px; color: #6b7280;">号码</label>
          <input id="playerNumber" type="number" placeholder="号码" min="0" />
        </div>
        <button type="button" onclick="handleAddPlayer()">添加</button>
      </div>
    </div>
    <div class="player-columns">
      <div class="player-group">
        <h3>${match.homeTeam.teamName}（主队）</h3>
        <ul>
          ${homePlayers.length ? homePlayers.map(p => `
            <li><span class="player-number">#${p.number}</span>${p.playerName}</li>
          `).join('') : '<li class="empty">暂无球员</li>'}
        </ul>
      </div>
      <div class="player-group">
        <h3>${match.awayTeam.teamName}（客队）</h3>
        <ul>
          ${awayPlayers.length ? awayPlayers.map(p => `
            <li><span class="player-number">#${p.number}</span>${p.playerName}</li>
          `).join('') : '<li class="empty">暂无球员</li>'}
        </ul>
      </div>
    </div>
  `;
}

function renderStatusActions() {
  const match = state.currentMatch;
  const container = document.querySelector('#statusActions');

  if (!match) {
    container.innerHTML = '<p class="empty">请先选择比赛。</p>';
    return;
  }

  const status = match.status;
  let buttons = '';

  switch (status) {
    case 'NOT_STARTED':
      buttons = `
        <button class="btn-success" onclick="changeMatchStatus('start')">开始比赛</button>
        <button disabled>暂停比赛</button>
        <button disabled>恢复比赛</button>
        <button class="btn-danger" disabled>结束比赛</button>
      `;
      break;
    case 'RUNNING':
      buttons = `
        <button class="btn-success" disabled>开始比赛</button>
        <button class="btn-warning" onclick="changeMatchStatus('pause')">暂停比赛</button>
        <button disabled>恢复比赛</button>
        <button class="btn-danger" onclick="changeMatchStatus('finish')">结束比赛</button>
      `;
      break;
    case 'PAUSED':
      buttons = `
        <button class="btn-success" disabled>开始比赛</button>
        <button class="btn-warning" disabled>暂停比赛</button>
        <button onclick="changeMatchStatus('resume')">恢复比赛</button>
        <button class="btn-danger" onclick="changeMatchStatus('finish')">结束比赛</button>
      `;
      break;
    case 'FINISHED':
      buttons = `
        <button class="btn-success" disabled>开始比赛</button>
        <button class="btn-warning" disabled>暂停比赛</button>
        <button disabled>恢复比赛</button>
        <button class="btn-danger" disabled>结束比赛</button>
      `;
      break;
  }

  container.innerHTML = `<div class="status-actions">${buttons}</div>`;
}

function renderEventForm() {
  const match = state.currentMatch;
  const container = document.querySelector('#eventFormArea');
  const editing = state.editingEvent;

  if (!match) {
    container.innerHTML = '<p class="empty">请先选择比赛。</p>';
    return;
  }

  if (!match.homeTeam || !match.awayTeam) {
    container.innerHTML = '<p class="empty">请先设置主客队。</p>';
    return;
  }

  const homePlayers = match.homeTeam.players || [];
  const awayPlayers = match.awayTeam.players || [];
  const eventTypes = EVENT_TYPES_BY_SPORT[match.sportType] || EVENT_TYPES_BY_SPORT.GENERAL;

  if (editing) {
    const isHome = editing.teamId === match.homeTeam?.teamId;
    const teamPlayers = isHome ? homePlayers : awayPlayers;
    const timeStr = editing.eventTime
      ? new Date(editing.eventTime).toISOString().slice(0, 16)
      : '';

    container.innerHTML = `
      <h3 style="font-size: 15px; margin-bottom: 10px; color: #2563eb;">修改事件</h3>
      <div class="event-form-grid">
        <div class="form-group">
          <label>队伍</label>
          <select id="editEventTeam" onchange="updateEditEventPlayerOptions()">
            <option value="${match.homeTeam.teamId}" ${isHome ? 'selected' : ''}>${match.homeTeam.teamName}（主队）</option>
            <option value="${match.awayTeam.teamId}" ${!isHome ? 'selected' : ''}>${match.awayTeam.teamName}（客队）</option>
          </select>
        </div>
        <div class="form-group">
          <label>球员</label>
          <select id="editEventPlayer">
            ${teamPlayers.map(p => `<option value="${p.playerId}" ${p.playerId === editing.playerId ? 'selected' : ''}>${p.playerName} (#${p.number})</option>`).join('')}
          </select>
        </div>
        <div class="form-group">
          <label>事件类型</label>
          <select id="editEventType">
            ${eventTypes.map(([val, label]) => `<option value="${val}" ${val === editing.eventType ? 'selected' : ''}>${label}</option>`).join('')}
          </select>
        </div>
        <div class="form-group">
          <label>得分值</label>
          <input id="editScoreValue" type="number" value="${editing.scoreValue}" min="0" />
        </div>
        <div class="form-group">
          <label>事件时间</label>
          <input id="editEventTime" type="datetime-local" value="${timeStr}" />
        </div>
        <div class="form-group full-width">
          <label>描述</label>
          <input id="editEventDescription" placeholder="例如：三分命中" value="${editing.description || ''}" />
        </div>
        <div class="full-width" style="display: flex; gap: 10px;">
          <button type="button" class="btn-block" onclick="handleSaveEvent('${editing.eventId}')">保存修改</button>
          <button type="button" class="btn-warning" onclick="handleCancelEventForm()">取消</button>
        </div>
      </div>
    `;
  } else {
    container.innerHTML = `
      <div class="event-form-grid">
        <div class="form-group">
          <label>队伍</label>
          <select id="eventTeam" onchange="updateEventPlayerOptions()">
            <option value="${match.homeTeam.teamId}">${match.homeTeam.teamName}（主队）</option>
            <option value="${match.awayTeam.teamId}">${match.awayTeam.teamName}（客队）</option>
          </select>
        </div>
        <div class="form-group">
          <label>球员</label>
          <select id="eventPlayer">
            ${homePlayers.map(p => `<option value="${p.playerId}">${p.playerName} (#${p.number})</option>`).join('')}
          </select>
        </div>
        <div class="form-group">
          <label>事件类型</label>
          <select id="eventType">
            ${eventTypes.map(([val, label]) => `<option value="${val}">${label}</option>`).join('')}
          </select>
        </div>
        <div class="form-group">
          <label>得分值</label>
          <input id="scoreValue" type="number" value="0" min="0" />
        </div>
        <div class="form-group full-width">
          <label>描述</label>
          <input id="eventDescription" placeholder="例如：三分命中" />
        </div>
        <div class="full-width">
          <button type="button" class="btn-block" onclick="handleRecordEvent()">提交事件</button>
        </div>
      </div>
    `;
  }
}

function renderEventTimeline() {
  const match = state.currentMatch;
  const events = match?.events || [];
  const container = document.querySelector('#eventTimeline');

  if (!events.length) {
    container.innerHTML = '<p class="empty">暂无事件记录。</p>';
    return;
  }

  const allPlayers = [
    ...((match.homeTeam?.players) || []),
    ...((match.awayTeam?.players) || [])
  ];

  const sportType = match.sportType;
  const eventTypes = EVENT_TYPES_BY_SPORT[sportType] || EVENT_TYPES_BY_SPORT.GENERAL;
  const filter = state.eventTypeFilter;

  const filteredEvents = filter
    ? events.filter(e => e.eventType === filter)
    : events;

  const filterHtml = `
    <div class="event-filter-bar">
      <label>按类型筛选：</label>
      <select onchange="handleEventTypeFilter(this)">
        <option value="">全部事件</option>
        ${eventTypes.map(([val, label]) =>
          `<option value="${val}" ${filter === val ? 'selected' : ''}>${label}</option>`
        ).join('')}
      </select>
      <span class="filter-count">共 ${filteredEvents.length} 条</span>
    </div>
  `;

  if (!filteredEvents.length) {
    container.innerHTML = filterHtml + '<p class="empty">没有符合条件的事件。</p>';
    return;
  }

  const timelineHtml = `
    <div class="timeline">
      ${filteredEvents.slice().reverse().map(event => {
        const player = allPlayers.find(p => p.playerId === event.playerId);
        const playerName = player ? player.playerName : '未知球员';
        const eventTypeLabel = EVENT_TYPE_LABELS[event.eventType] || event.eventType;
        const timeStr = event.eventTime ? new Date(event.eventTime).toLocaleString('zh-CN') : '';

        return `
          <div class="timeline-item">
            <div class="timeline-item-header">
              <span class="event-type">${eventTypeLabel}</span>
              ${event.scoreValue > 0 ? `<span class="event-score">+${event.scoreValue}</span>` : ''}
              <div class="event-actions">
                <button class="btn-sm" onclick="handleEditEvent('${event.eventId}')">修改</button>
                <button class="btn-sm btn-danger" onclick="handleDeleteEvent('${event.eventId}')">删除</button>
              </div>
            </div>
            <div class="event-desc">${playerName} — ${event.description || '无描述'}</div>
            <div class="event-time">${timeStr}</div>
          </div>
        `;
      }).join('')}
    </div>
  `;

  container.innerHTML = filterHtml + timelineHtml;
}

function renderStatistics() {
  const stats = state.statistics;
  const container = document.querySelector('#statisticsPanel');

  if (!stats) {
    container.innerHTML = '<p class="empty">暂无统计数据，请先录入事件。</p>';
    return;
  }

  const match = state.currentMatch;
  const allPlayers = [
    ...((match?.homeTeam?.players) || []),
    ...((match?.awayTeam?.players) || [])
  ];

  let playerStatsHtml = '';
  if (stats.playerStats && Object.keys(stats.playerStats).length > 0) {
    const rows = Object.entries(stats.playerStats).map(([playerId, statMap]) => {
      const player = allPlayers.find(p => p.playerId === playerId);
      const name = player ? `${player.playerName} (#${player.number})` : playerId;
      const cells = Object.entries(statMap).map(([key, val]) => `<td>${val}</td>`).join('');
      return `<tr><td>${name}</td>${cells}</tr>`;
    }).join('');

    const statKeys = Object.values(stats.playerStats).flatMap(m => Object.keys(m));
    const uniqueKeys = [...new Set(statKeys)];
    const headerCells = uniqueKeys.map(k => `<th>${EVENT_TYPE_LABELS[k] || k}</th>`).join('');

    playerStatsHtml = `
      <h3 style="font-size: 15px; margin: 16px 0 8px;">球员统计</h3>
      <table class="stats-table">
        <thead><tr><th>球员</th>${headerCells}</tr></thead>
        <tbody>${rows}</tbody>
      </table>
    `;
  }

  let teamStatsHtml = '';
  if (stats.teamStats && Object.keys(stats.teamStats).length > 0) {
    const rows = Object.entries(stats.teamStats).map(([key, val]) => {
      return `<tr><td>${key}</td><td>${val}</td></tr>`;
    }).join('');

    teamStatsHtml = `
      <h3 style="font-size: 15px; margin: 16px 0 8px;">球队统计</h3>
      <table class="stats-table">
        <thead><tr><th>统计项</th><th>数值</th></tr></thead>
        <tbody>${rows}</tbody>
      </table>
    `;
  }

  container.innerHTML = `
    <div class="stats-grid">
      <div class="stat-item">
        <div class="stat-value">${stats.homeScore}</div>
        <div class="stat-label">主队得分</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">${stats.awayScore}</div>
        <div class="stat-label">客队得分</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">${stats.eventCount}</div>
        <div class="stat-label">事件总数</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">${stats.leadingTeam || '平局'}</div>
        <div class="stat-label">领先方</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">${stats.scoreDifference}</div>
        <div class="stat-label">分差</div>
      </div>
    </div>
    ${teamStatsHtml}
    ${playerStatsHtml}
  `;
}

function renderAnalysis() {
  const container = document.querySelector('#analysisText');
  if (state.analysis) {
    container.innerHTML = marked.parse(preprocessMarkdown(state.analysis));
  } else if (state.eventLogs.length > 0) {
    container.innerHTML = '<div class="event-log-list">' +
      state.eventLogs.map(log => `<div class="event-log-line">${log}</div>`).join('') +
      '</div>';
  } else {
    container.innerHTML = '<p>点击上方按钮生成实时态势分析。</p>';
  }
}

function renderReport() {
  const container = document.querySelector('#reportText');
  if (state.report) {
    container.innerHTML = marked.parse(preprocessMarkdown(state.report));
  } else {
    container.innerHTML = '<p>点击上方按钮生成赛后复盘报告。</p>';
  }
}

// ==================== 球员管理渲染 ====================

function renderPlayerTeamFilter() {
  const select = document.querySelector('#playerTeamFilter');
  select.innerHTML = '<option value="">全部队伍</option>' +
    state.teams.map(t => `<option value="${t.teamId}" ${state.playerFilterTeamId === t.teamId ? 'selected' : ''}>${t.teamName}</option>`).join('');
}

function renderPlayerList() {
  const container = document.querySelector('#playerList');
  const players = state.players;

  if (!players.length) {
    container.innerHTML = '<p class="empty">暂无球员。</p>';
    return;
  }

  container.innerHTML = players.map(player => {
    const team = state.teams.find(t => t.teamId === player.teamId);
    const teamName = team ? team.teamName : '未知队伍';
    return `
      <div class="match-item ${state.editingPlayer?.playerId === player.playerId ? 'active' : ''}"
           onclick="selectPlayer('${player.playerId}')">
        <span class="match-item-name">#${player.number} ${player.playerName}</span>
        <div class="match-item-meta">
          <span>${teamName}</span>
          <span>${player.position || '-'}</span>
        </div>
      </div>
    `;
  }).join('');
}

function renderPlayerDetail() {
  const container = document.querySelector('#playerDetail');
  const player = state.editingPlayer;

  if (!player) {
    container.innerHTML = '<p class="empty">请从左侧选择一位球员。</p>';
    return;
  }

  const team = state.teams.find(t => t.teamId === player.teamId);
  const teamName = team ? team.teamName : '未知队伍';

  container.innerHTML = `
    <div class="player-detail-info">
      <div class="overview-info">
        <div class="info-item"><strong>球员姓名：</strong>${player.playerName}</div>
        <div class="info-item"><strong>球衣号码：</strong>#${player.number}</div>
        <div class="info-item"><strong>所属队伍：</strong>${teamName}</div>
        <div class="info-item"><strong>场上位置：</strong>${player.position || '-'}</div>
        <div class="info-item"><strong>年龄：</strong>${player.age || '-'}</div>
        <div class="info-item"><strong>身高：</strong>${player.height ? player.height + ' cm' : '-'}</div>
        <div class="info-item"><strong>体重：</strong>${player.weight ? player.weight + ' kg' : '-'}</div>
      </div>
      <div style="margin-top: 16px; display: flex; gap: 10px;">
        <button onclick="handleEditPlayer()">编辑</button>
        <button class="btn-danger" onclick="handleDeletePlayer('${player.playerId}')">删除</button>
      </div>
    </div>
  `;
}

function renderPlayerForm(player) {
  const card = document.querySelector('#playerFormCard');
  const title = document.querySelector('#playerFormTitle');
  const container = document.querySelector('#playerFormArea');

  card.style.display = 'block';
  title.textContent = player ? '编辑球员' : '新增球员';

  const isEdit = !!player;

  container.innerHTML = `
    <div class="event-form-grid">
      <div class="form-group">
        <label>球员姓名</label>
        <input id="pf_playerName" placeholder="球员姓名" value="${player?.playerName || ''}" />
      </div>
      <div class="form-group">
        <label>球衣号码</label>
        <input id="pf_number" type="number" min="1" placeholder="球衣号码" value="${player?.number || ''}" />
      </div>
      <div class="form-group">
        <label>所属队伍</label>
        <select id="pf_teamId" ${isEdit ? 'disabled' : ''}>
          <option value="">请选择队伍</option>
          ${state.teams.map(t => `<option value="${t.teamId}" ${player?.teamId === t.teamId ? 'selected' : ''}>${t.teamName}</option>`).join('')}
        </select>
      </div>
      <div class="form-group">
        <label>场上位置</label>
        <input id="pf_position" placeholder="例如：后卫" value="${player?.position || ''}" />
      </div>
      <div class="form-group">
        <label>年龄</label>
        <input id="pf_age" type="number" min="0" placeholder="年龄" value="${player?.age || ''}" />
      </div>
      <div class="form-group">
        <label>身高 (cm)</label>
        <input id="pf_height" type="number" min="0" placeholder="身高" value="${player?.height || ''}" />
      </div>
      <div class="form-group">
        <label>体重 (kg)</label>
        <input id="pf_weight" type="number" min="0" placeholder="体重" value="${player?.weight || ''}" />
      </div>
      <div class="full-width" style="display: flex; gap: 10px;">
        <button class="btn-block" onclick="handleSavePlayer(${isEdit ? `'${player.playerId}'` : 'null'})">
          ${isEdit ? '保存修改' : '添加球员'}
        </button>
        <button class="btn-warning" onclick="handleCancelPlayerForm()">取消</button>
      </div>
    </div>
  `;
}

function renderPlayerEvents() {
  const container = document.querySelector('#playerEventsPanel');
  const events = state.playerEvents;

  if (!events || !events.length) {
    container.innerHTML = '<p class="empty">该球员暂无比赛事件记录。</p>';
    return;
  }

  container.innerHTML = `
    <div class="timeline">
      ${events.slice().reverse().map(event => {
        const eventTypeLabel = EVENT_TYPE_LABELS[event.eventType] || event.eventType;
        const timeStr = event.eventTime ? new Date(event.eventTime).toLocaleString('zh-CN') : '';
        return `
          <div class="timeline-item">
            <span class="event-type">${eventTypeLabel}</span>
            ${event.scoreValue > 0 ? `<span class="event-score">+${event.scoreValue}</span>` : ''}
            <div class="event-desc">${event.description || '无描述'}</div>
            <div class="event-time">${timeStr}</div>
          </div>
        `;
      }).join('')}
    </div>
  `;
}

function renderPlayerStatistics() {
  const container = document.querySelector('#playerStatsPanel');
  const stats = state.playerStatistics;

  if (!stats || !stats.length) {
    container.innerHTML = '<p class="empty">该球员暂无统计数据。</p>';
    return;
  }

  const byMatch = {};
  stats.forEach(s => {
    if (!byMatch[s.matchId]) byMatch[s.matchId] = [];
    byMatch[s.matchId].push(s);
  });

  let html = '';
  Object.entries(byMatch).forEach(([matchId, matchStats]) => {
    const rows = matchStats.map(s => {
      const label = EVENT_TYPE_LABELS[s.statKey] || s.statKey;
      return `<tr><td>${label}</td><td>${s.statValue}</td></tr>`;
    }).join('');

    html += `
      <h3 style="font-size: 15px; margin: 16px 0 8px;">比赛: ${matchId}</h3>
      <table class="stats-table">
        <thead><tr><th>统计项</th><th>数值</th></tr></thead>
        <tbody>${rows}</tbody>
      </table>
    `;
  });

  container.innerHTML = html;
}
