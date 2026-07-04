// MatchLens 登录/注册逻辑

const API_BASE = '';

function showToast(msg, duration = 2500) {
  const el = document.getElementById('toast');
  el.textContent = msg;
  el.classList.add('show');
  setTimeout(() => el.classList.remove('show'), duration);
}

function showError(msg) {
  const el = document.getElementById('authError');
  el.textContent = msg;
  el.classList.add('show');
}

function hideError() {
  document.getElementById('authError').classList.remove('show');
}

function switchTab(tab) {
  hideError();
  // 更新 tab 样式
  document.querySelectorAll('.auth-tab').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.auth-form').forEach(f => f.classList.remove('active'));

  if (tab === 'login') {
    document.querySelectorAll('.auth-tab')[0].classList.add('active');
    document.getElementById('loginForm').classList.add('active');
  } else {
    document.querySelectorAll('.auth-tab')[1].classList.add('active');
    document.getElementById('registerForm').classList.add('active');
  }
}

async function handleLogin(e) {
  e.preventDefault();
  hideError();
  const username = document.getElementById('loginUsername').value.trim();
  const password = document.getElementById('loginPassword').value;

  try {
    const resp = await fetch(API_BASE + '/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    const data = await resp.json();
    if (!data.success) {
      showError(data.message || '登录失败');
      return;
    }
    // 存储 token 和用户信息
    const user = data.data;
    localStorage.setItem('token', user.token);
    localStorage.setItem('userId', user.userId);
    localStorage.setItem('username', user.username);
    localStorage.setItem('nickname', user.nickname);
    localStorage.setItem('role', user.role);

    showToast('登录成功！');
    // 根据角色跳转
    setTimeout(() => {
      if (user.role === 'ADMIN') {
        window.location.href = './index.html';
      } else {
        window.location.href = './client-index.html';
      }
    }, 500);
  } catch (err) {
    showError('网络错误，请稍后重试');
    console.error(err);
  }
}

async function handleRegister(e) {
  e.preventDefault();
  hideError();
  const username = document.getElementById('regUsername').value.trim();
  const nickname = document.getElementById('regNickname').value.trim();
  const password = document.getElementById('regPassword').value;
  const confirmPassword = document.getElementById('regConfirmPassword').value;

  if (password !== confirmPassword) {
    showError('两次输入的密码不一致');
    return;
  }

  try {
    const resp = await fetch(API_BASE + '/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password, nickname })
    });
    const data = await resp.json();
    if (!data.success) {
      showError(data.message || '注册失败');
      return;
    }
    showToast('注册成功，请登录！');
    // 切换到登录 tab
    setTimeout(() => switchTab('login'), 800);
    document.getElementById('loginUsername').value = username;
  } catch (err) {
    showError('网络错误，请稍后重试');
    console.error(err);
  }
}

// 如果已登录，直接跳转
(function checkLoggedIn() {
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');
  if (token && role) {
    if (role === 'ADMIN') {
      window.location.href = './index.html';
    } else {
      window.location.href = './client-index.html';
    }
  }
})();
