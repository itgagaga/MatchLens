const state = {
  matches: [],
  currentMatch: null,
  statistics: null,
  analysis: '',
  report: '',
  eventLogs: [],
  players: [],
  teams: [],
  playerFilterTeamId: '',
  editingPlayer: null,
  playerEvents: [],
  playerStatistics: [],
  editingEvent: null,
  eventTypeFilter: ''
};

document.addEventListener('DOMContentLoaded', () => {
  initSportTypeSelect();
  bindEvents();
  loadMatches();
});

function initSportTypeSelect() {
  const select = document.querySelector('#sportType');
  select.innerHTML = SPORT_TYPES.map(s =>
    `<option value="${s.value}">${s.label}</option>`
  ).join('');
}

function bindEvents() {
  document.querySelector('#createMatchForm').addEventListener('submit', handleCreateMatch);
  document.querySelector('#refreshBtn').addEventListener('click', () => {
    if (state.currentMatch) {
      selectMatch(state.currentMatch.matchId);
    } else {
      loadMatches();
    }
  });
  document.querySelector('#refreshListBtn').addEventListener('click', loadMatches);
  document.querySelector('#analysisBtn').addEventListener('click', refreshAnalysis);
  document.querySelector('#reportBtn').addEventListener('click', generateReport);
}

async function loadMatches() {
  try {
    state.matches = await api.getMatches();
    renderMatchList();

    if (!state.currentMatch && state.matches.length > 0) {
      await selectMatch(state.matches[0].matchId);
    }
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function selectMatch(matchId) {
  try {
    const isSwitchingMatch = !state.currentMatch || state.currentMatch.matchId !== matchId;
    state.currentMatch = await api.getMatch(matchId);
    state.statistics = null;
    state.analysis = '';
    state.report = '';

    if (isSwitchingMatch) {
      state.eventLogs = [];
      state.editingEvent = null;
      state.eventTypeFilter = '';
    }

    try {
      state.statistics = await api.getStatistics(matchId);
    } catch (e) {
      // statistics may fail if no events yet
    }

    renderAll();
    renderAnalysis();
    renderReport();
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function handleCreateMatch(event) {
  event.preventDefault();

  const matchName = document.querySelector('#matchName').value.trim();
  const sportType = document.querySelector('#sportType').value;

  if (!matchName) {
    showToast('请输入比赛名称', 'warning');
    return;
  }

  try {
    const match = await api.createMatch({ matchName, sportType });
    showToast('比赛创建成功', 'success');
    document.querySelector('#matchName').value = '';
    await loadMatches();
    await selectMatch(match.matchId);
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function handleSetTeams() {
  if (!state.currentMatch) {
    showToast('请先选择比赛', 'warning');
    return;
  }

  const homeName = document.querySelector('#homeName').value.trim();
  const awayName = document.querySelector('#awayName').value.trim();

  if (!homeName || !awayName) {
    showToast('请输入主队和客队名称', 'warning');
    return;
  }

  try {
    const result = await api.setTeams(state.currentMatch.matchId, { homeName, awayName });
    showResult(result);
    await selectMatch(state.currentMatch.matchId);
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function handleAddPlayer() {
  if (!state.currentMatch) {
    showToast('请先选择比赛', 'warning');
    return;
  }

  const home = document.querySelector('#playerTeam').value === 'home';
  const playerName = document.querySelector('#playerName').value.trim();
  const number = Number(document.querySelector('#playerNumber').value);

  if (!playerName) {
    showToast('请输入球员姓名', 'warning');
    return;
  }

  if (isNaN(number) || number < 0) {
    showToast('请输入有效的球衣号码', 'warning');
    return;
  }

  try {
    const result = await api.addPlayer(state.currentMatch.matchId, {
      home,
      playerName,
      number
    });
    showResult(result);
    await selectMatch(state.currentMatch.matchId);
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function changeMatchStatus(action) {
  if (!state.currentMatch) return;

  try {
    const result = await api.changeStatus(state.currentMatch.matchId, action);
    showResult(result);
    await selectMatch(state.currentMatch.matchId);
  } catch (error) {
    showToast(error.message, 'error');
  }
}

function updateEventPlayerOptions() {
  const match = state.currentMatch;
  if (!match) return;

  const teamId = document.querySelector('#eventTeam').value;
  const playerSelect = document.querySelector('#eventPlayer');

  let players = [];
  if (teamId === match.homeTeam?.teamId) {
    players = match.homeTeam.players || [];
  } else {
    players = match.awayTeam.players || [];
  }

  playerSelect.innerHTML = players.map(p =>
    `<option value="${p.playerId}">${p.playerName} (#${p.number})</option>`
  ).join('');
}

async function handleRecordEvent() {
  if (!state.currentMatch) {
    showToast('请先选择比赛', 'warning');
    return;
  }

  const teamId = document.querySelector('#eventTeam').value;
  const playerId = document.querySelector('#eventPlayer').value;
  const eventType = document.querySelector('#eventType').value;
  const scoreValue = Number(document.querySelector('#scoreValue').value || 0);
  const description = document.querySelector('#eventDescription').value.trim();

  if (!playerId) {
    showToast('请选择球员', 'warning');
    return;
  }

  try {
    const result = await api.recordEvent(state.currentMatch.matchId, {
      teamId,
      playerId,
      eventType,
      scoreValue,
      description
    });

    showResult(result);

    if (result.success) {
      appendEventLog(teamId, playerId, eventType, scoreValue);

      document.querySelector('#scoreValue').value = '0';
      document.querySelector('#eventDescription').value = '';

      await selectMatch(state.currentMatch.matchId);
    }
  } catch (error) {
    showToast(error.message, 'error');
  }
}

function appendEventLog(teamId, playerId, eventType, scoreValue) {
  const match = state.currentMatch;
  if (!match) return;

  const allPlayers = [
    ...((match.homeTeam?.players) || []),
    ...((match.awayTeam?.players) || [])
  ];
  const player = allPlayers.find(p => p.playerId === playerId);
  const playerName = player ? player.playerName : '未知球员';

  const isHome = match.homeTeam?.teamId === teamId;
  const teamName = isHome ? match.homeTeam.teamName : match.awayTeam?.teamName || '未知队伍';

  const eventTypeLabel = EVENT_TYPE_LABELS[eventType] || eventType;
  const homeScore = match.homeTeam?.score || 0;
  const awayScore = match.awayTeam?.score || 0;

  const logs = [];

  if (eventType === 'SCORE' && scoreValue > 0) {
    logs.push(`<span class="log-prefix">[比分板]</span> ${playerName} 得分 +${scoreValue} | ${match.homeTeam.teamName} ${homeScore} : ${awayScore} ${match.awayTeam.teamName}`);
  }

  logs.push(`<span class="log-prefix">[统计更新]</span> 事件: ${eventTypeLabel} | 球员: ${playerName} | 当前比分: ${homeScore}:${awayScore}`);
  logs.push(`<span class="log-prefix">[复盘记录]</span> 记录事件: ${eventTypeLabel} (比赛: ${match.matchName})`);

  state.eventLogs.push(...logs);
  renderEventLogs();
}

function renderEventLogs() {
  const container = document.querySelector('#analysisText');
  if (!container) return;

  if (state.eventLogs.length === 0 && !state.analysis) {
    container.innerHTML = '<p>点击上方按钮生成实时态势分析。</p>';
    return;
  }

  if (state.analysis) return;

  container.innerHTML = '<div class="event-log-list">' +
    state.eventLogs.map(log => `<div class="event-log-line">${log}</div>`).join('') +
    '</div>';
  container.scrollTop = container.scrollHeight;
}

async function refreshAnalysis() {
  if (!state.currentMatch) return;

  const btn = document.querySelector('#analysisBtn');
  const container = document.querySelector('#analysisText');
  btn.disabled = true;
  btn.textContent = '正在生成...';
  container.innerHTML = '<span class="loading-dots">AI 正在分析中</span>';

  let accumulated = '';

  api.streamAnalysis(
    state.currentMatch.matchId,
    (chunk) => {
      accumulated += chunk;
      container.innerHTML = marked.parse(preprocessMarkdown(accumulated));
      container.scrollTop = container.scrollHeight;
    },
    () => {
      state.analysis = accumulated;
      btn.disabled = false;
      btn.textContent = '生成 / 刷新态势分析';
    },
    (error) => {
      showToast('获取态势分析失败：' + error, 'error');
      container.innerHTML = '<p>生成失败，请重试。</p>';
      btn.disabled = false;
      btn.textContent = '生成 / 刷新态势分析';
    }
  );
}

async function generateReport() {
  if (!state.currentMatch) return;

  const btn = document.querySelector('#reportBtn');
  const container = document.querySelector('#reportText');
  btn.disabled = true;
  btn.textContent = '正在生成...';
  container.innerHTML = '<span class="loading-dots">AI 正在生成复盘报告</span>';

  let accumulated = '';

  api.streamReport(
    state.currentMatch.matchId,
    (chunk) => {
      accumulated += chunk;
      container.innerHTML = marked.parse(preprocessMarkdown(accumulated));
      container.scrollTop = container.scrollHeight;
    },
    () => {
      state.report = accumulated;
      btn.disabled = false;
      btn.textContent = '生成赛后复盘';
    },
    (error) => {
      showToast('生成赛后复盘失败：' + error, 'error');
      container.innerHTML = '<p>生成失败，请重试。</p>';
      btn.disabled = false;
      btn.textContent = '生成赛后复盘';
    }
  );
}

function preprocessMarkdown(text) {
  if (!text) return text;
  const lines = text.split('\n');
  const result = [];
  const cnNums = '一二三四五六七八九十';

  for (let i = 0; i < lines.length; i++) {
    let line = lines[i].trim();
    if (!line) { result.push(''); continue; }

    if (/^#{1,6}\s/.test(line)) {
      result.push('');
      result.push(line);
      result.push('');
      continue;
    }

    if (/^\*\*[^*]+\*\*\s*$/.test(line)) {
      result.push('');
      let heading = line.replace(/^\*\*([^*]+)\*\*/, '$1');
      result.push('## ' + heading);
      result.push('');
      continue;
    }

    if (/^\*\*\d+[、.．]\s*[^*]+\*\*\s*$/.test(line)) {
      result.push('');
      let heading = line.replace(/^\*\*(\d+)([、.．])\s*([^*]+)\*\*/, '$1$2 $3');
      result.push('## ' + heading);
      result.push('');
      continue;
    }

    if (new RegExp('^\\*\\*[' + cnNums + ']+[、.．]\\s*[^*]+\\*\\*\\s*$').test(line)) {
      result.push('');
      let heading = line.replace(/^\*\*([^*]+)\*\*/, '$1');
      result.push('## ' + heading);
      result.push('');
      continue;
    }

    if (/^[-*]\s+/.test(line)) {
      result.push(line);
      continue;
    }

    if (/^\d+[、.．]\s+/.test(line) && line.length < 100) {
      result.push('- ' + line.replace(/^\d+[、.．]\s+/, ''));
      continue;
    }

    if (/^[（(]\d+[)）]\s+/.test(line) && line.length < 100) {
      result.push('- ' + line.replace(/^[（(]\d+[)）]\s+/, ''));
      continue;
    }

    result.push(line);
  }

  let output = result.join('\n');
  output = output.replace(/\n{3,}/g, '\n\n');
  return output;
}

function showToast(message, type = 'info') {
  const toast = document.querySelector('#toast');
  toast.textContent = message;
  toast.className = `toast show ${type}`;

  setTimeout(() => {
    toast.className = 'toast';
  }, 2800);
}

function showResult(result) {
  if (!result) return;

  if (result.success) {
    showToast(result.message || result.data || '操作成功', 'success');
  } else {
    showToast(result.message || '操作失败', 'error');
  }
}

// ==================== 事件管理 ====================

function handleEventTypeFilter(select) {
  state.eventTypeFilter = select.value;
  renderEventTimeline();
}

function handleEditEvent(eventId) {
  const match = state.currentMatch;
  if (!match) return;
  const event = match.events.find(e => e.eventId === eventId);
  if (!event) return;
  state.editingEvent = event;
  renderEventForm();
}

function handleCancelEventForm() {
  state.editingEvent = null;
  renderEventForm();
}

async function handleSaveEvent(eventId) {
  const match = state.currentMatch;
  if (!match) return;

  const teamId = document.querySelector('#editEventTeam').value;
  const playerId = document.querySelector('#editEventPlayer').value;
  const eventType = document.querySelector('#editEventType').value;
  const scoreValue = Number(document.querySelector('#editScoreValue').value || 0);
  const eventTime = document.querySelector('#editEventTime').value;
  const description = document.querySelector('#editEventDescription').value.trim();

  if (!playerId) {
    showToast('请选择球员', 'warning');
    return;
  }

  try {
    const data = { teamId, playerId, eventType, scoreValue, description };
    if (eventTime) {
      data.eventTime = eventTime;
    }

    const result = await api.updateEvent(eventId, data);
    showResult(result);

    if (result.success) {
      state.editingEvent = null;
      await selectMatch(match.matchId);
    }
  } catch (error) {
    showToast('修改事件失败：' + error.message, 'error');
  }
}

async function handleDeleteEvent(eventId) {
  if (!confirm('确定要删除该事件吗？删除后将重新计算比分和统计。')) return;

  try {
    const result = await api.deleteEvent(eventId);
    showResult(result);

    if (result.success && state.currentMatch) {
      state.editingEvent = null;
      await selectMatch(state.currentMatch.matchId);
    }
  } catch (error) {
    showToast('删除事件失败：' + error.message, 'error');
  }
}

function updateEditEventPlayerOptions() {
  const match = state.currentMatch;
  if (!match) return;

  const teamId = document.querySelector('#editEventTeam').value;
  const playerSelect = document.querySelector('#editEventPlayer');

  let players = [];
  if (teamId === match.homeTeam?.teamId) {
    players = match.homeTeam.players || [];
  } else {
    players = match.awayTeam.players || [];
  }

  playerSelect.innerHTML = players.map(p =>
    `<option value="${p.playerId}">${p.playerName} (#${p.number})</option>`
  ).join('');
}

// ==================== 球员管理 ====================

function switchTab(tab) {
  console.log('switchTab 被调用:', tab);
  const matchSection = document.querySelector('#matchSection');
  const playerMgmtSection = document.querySelector('#playerMgmtSection');
  const tabMatchBtn = document.querySelector('#tabMatchBtn');
  const tabPlayerBtn = document.querySelector('#tabPlayerBtn');

  console.log('matchSection:', matchSection);
  console.log('playerMgmtSection:', playerMgmtSection);

  if (tab === 'match') {
    matchSection.style.display = '';
    playerMgmtSection.style.display = 'none';
    tabMatchBtn.classList.add('active');
    tabPlayerBtn.classList.remove('active');
  } else {
    matchSection.style.display = 'none';
    playerMgmtSection.style.display = '';
    tabMatchBtn.classList.remove('active');
    tabPlayerBtn.classList.add('active');
    loadPlayerManagement();
  }
}

async function loadPlayerManagement() {
  try {
    console.log('loadPlayerManagement 开始');
    state.teams = await api.getTeams();
    console.log('teams 加载成功:', state.teams);
    renderPlayerTeamFilter();
    await loadPlayers();
    console.log('loadPlayerManagement 完成');
  } catch (error) {
    console.error('loadPlayerManagement 错误:', error);
    showToast('加载球员管理数据失败：' + error.message, 'error');
  }
}

async function loadPlayers() {
  try {
    console.log('loadPlayers 开始, teamId:', state.playerFilterTeamId);
    state.players = await api.getPlayers(state.playerFilterTeamId || undefined);
    console.log('players 加载成功:', state.players);
    renderPlayerList();
  } catch (error) {
    console.error('loadPlayers 错误:', error);
    showToast('加载球员列表失败：' + error.message, 'error');
  }
}

async function selectPlayer(playerId) {
  try {
    state.editingPlayer = await api.getPlayer(playerId);
    state.playerEvents = [];
    state.playerStatistics = [];

    renderPlayerDetail();
    renderPlayerList();

    const [events, stats] = await Promise.all([
      api.getPlayerEvents(playerId).catch(() => []),
      api.getPlayerStatistics(playerId).catch(() => [])
    ]);

    state.playerEvents = events;
    state.playerStatistics = stats;
    renderPlayerEvents();
    renderPlayerStatistics();
  } catch (error) {
    showToast('加载球员详情失败：' + error.message, 'error');
  }
}

async function handlePlayerTeamFilter() {
  state.playerFilterTeamId = document.querySelector('#playerTeamFilter').value;
  state.editingPlayer = null;
  state.playerEvents = [];
  state.playerStatistics = [];
  renderPlayerDetail();
  renderPlayerEvents();
  renderPlayerStatistics();
  await loadPlayers();
}

function handleShowAddPlayer() {
  state.editingPlayer = null;
  renderPlayerForm(null);
}

function handleEditPlayer() {
  if (!state.editingPlayer) return;
  renderPlayerForm(state.editingPlayer);
}

function handleCancelPlayerForm() {
  const card = document.querySelector('#playerFormCard');
  card.style.display = 'none';
}

async function handleSavePlayer(playerId) {
  const playerName = document.querySelector('#pf_playerName').value.trim();
  const number = Number(document.querySelector('#pf_number').value);
  const position = document.querySelector('#pf_position').value.trim();
  const age = Number(document.querySelector('#pf_age').value) || 0;
  const height = Number(document.querySelector('#pf_height').value) || 0;
  const weight = Number(document.querySelector('#pf_weight').value) || 0;

  if (!playerName) {
    showToast('请输入球员姓名', 'warning');
    return;
  }

  if (isNaN(number) || number <= 0) {
    showToast('请输入有效的球衣号码（大于0）', 'warning');
    return;
  }

  try {
    let result;
    if (playerId) {
      result = await api.updatePlayer(playerId, {
        playerName, number, position, age, height, weight
      });
    } else {
      const teamId = document.querySelector('#pf_teamId').value;
      if (!teamId) {
        showToast('请选择所属队伍', 'warning');
        return;
      }
      result = await api.createPlayer({
        playerName, teamId, number, position, age, height, weight
      });
    }

    showResult(result);

    if (result.success) {
      handleCancelPlayerForm();
      await loadPlayers();
      if (playerId) {
        await selectPlayer(playerId);
      }
    }
  } catch (error) {
    showToast('操作失败：' + error.message, 'error');
  }
}

async function handleDeletePlayer(playerId) {
  if (!confirm('确定要删除该球员吗？')) return;

  try {
    const result = await api.deletePlayer(playerId);
    showResult(result);

    if (result.success) {
      state.editingPlayer = null;
      state.playerEvents = [];
      state.playerStatistics = [];
      renderPlayerDetail();
      renderPlayerEvents();
      renderPlayerStatistics();
      await loadPlayers();
    }
  } catch (error) {
    showToast('删除失败：' + error.message, 'error');
  }
}
