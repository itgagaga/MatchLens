const ClientProfilePage = {
  async init() {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    if (!token) { window.location.href = './login.html'; return; }
    if (role === 'ADMIN') { window.location.href = './matches.html'; return; }

    // 填充基本信息
    document.getElementById('usernameDisplay').value = localStorage.getItem('username') || '';
    document.getElementById('roleDisplay').value = '普通用户';
    document.getElementById('nicknameInput').value = localStorage.getItem('nickname') || '';
  },

  async saveNickname() {
    const nickname = document.getElementById('nicknameInput').value.trim();
    if (!nickname) {
      Common.toast('昵称不能为空');
      return;
    }
    try {
      await API.updateNickname(nickname);
      localStorage.setItem('nickname', nickname);
      Common.toast('昵称修改成功！');
      // 更新 topbar 下拉菜单显示
      const nameEl = document.querySelector('.user-name');
      if (nameEl) nameEl.textContent = nickname;
    } catch (e) {
      Common.toast(e.message || '修改失败');
    }
  },

  async changePassword() {
    const oldPassword = document.getElementById('oldPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (!oldPassword) { Common.toast('请输入当前密码'); return; }
    if (!newPassword || newPassword.length < 6) { Common.toast('新密码长度不能少于6位'); return; }
    if (newPassword !== confirmPassword) { Common.toast('两次输入的新密码不一致'); return; }

    try {
      await API.updatePassword(oldPassword, newPassword);
      Common.toast('密码修改成功，请重新登录！');
      // 清空表单
      document.getElementById('oldPassword').value = '';
      document.getElementById('newPassword').value = '';
      document.getElementById('confirmPassword').value = '';
      // 1.5秒后退出登录
      setTimeout(() => Common.logout(), 1500);
    } catch (e) {
      Common.toast(e.message || '修改失败');
    }
  }
};

document.addEventListener('DOMContentLoaded', () => ClientProfilePage.init());
