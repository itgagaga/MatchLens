const state = {
  matches: [],
  currentMatch: null,
  statistics: null,
  analysis: '',
  report: ''
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
    state.currentMatch = await api.getMatch(matchId);
    state.statistics = null;
    state.analysis = '';
    state.report = '';

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
      document.querySelector('#scoreValue').value = '0';
      document.querySelector('#eventDescription').value = '';

      await selectMatch(state.currentMatch.matchId);
      await refreshAnalysis();
    }
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function refreshAnalysis() {
  if (!state.currentMatch) return;

  const btn = document.querySelector('#analysisBtn');
  const container = document.querySelector('#analysisText');
  btn.disabled = true;
  btn.textContent = '正在生成...';
  container.innerHTML = '<span class="loading-dots">AI 正在分析中</span>';
  await new Promise(r => setTimeout(r, 50));

  try {
    state.analysis = cleanAiText(await api.getAnalysis(state.currentMatch.matchId));
    renderAnalysis();
  } catch (error) {
    showToast('获取态势分析失败：' + error.message, 'error');
    renderAnalysis();
  } finally {
    btn.disabled = false;
    btn.textContent = '生成 / 刷新态势分析';
  }
}

async function generateReport() {
  if (!state.currentMatch) return;

  const btn = document.querySelector('#reportBtn');
  const container = document.querySelector('#reportText');
  btn.disabled = true;
  btn.textContent = '正在生成...';
  container.innerHTML = '<span class="loading-dots">AI 正在生成复盘报告</span>';
  await new Promise(r => setTimeout(r, 50));

  try {
    state.report = cleanAiText(await api.getReport(state.currentMatch.matchId));
    renderReport();
  } catch (error) {
    showToast('生成赛后复盘失败：' + error.message, 'error');
    renderReport();
  } finally {
    btn.disabled = false;
    btn.textContent = '生成赛后复盘';
  }
}

function cleanAiText(text) {
  if (!text) return text;
  return text
    .replace(/\*\*/g, '')
    .replace(/^\s*\*\s+/gm, '')
    .replace(/(?<!\w)\*(?!\w)/g, '')
    .replace(/\n{3,}/g, '\n\n')
    .trim();
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
