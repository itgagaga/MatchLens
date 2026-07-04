package com.zzx.matchlens.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzx.matchlens.dto.LoginRequest;
import com.zzx.matchlens.dto.LoginResponse;
import com.zzx.matchlens.dto.RegisterRequest;
import com.zzx.matchlens.entity.User;
import com.zzx.matchlens.mapper.UserMapper;
import com.zzx.matchlens.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserService(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 用户注册（默认角色 USER）
     */
    public void register(RegisterRequest request) {
        // 校验必填字段
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }

        // 校验用户名唯一
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole("USER");
        userMapper.insert(user);
    }

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("密码不能为空");
        }

        // 查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 校验密码
        if (!request.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成 token
        String token = jwtUtil.generateToken(String.valueOf(user.getUserId()), user.getRole());

        return new LoginResponse(token, user.getUserId(), user.getUsername(), user.getNickname(), user.getRole());
    }

    /**
     * 根据 ID 获取用户（不返回密码）
     */
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    /**
     * 修改昵称
     */
    public void updateNickname(Long userId, String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new RuntimeException("昵称不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setNickname(nickname);
        userMapper.updateById(user);
    }

    /**
     * 修改密码
     */
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!user.getPassword().equals(oldPassword)) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(newPassword);
        userMapper.updateById(user);
    }
}
