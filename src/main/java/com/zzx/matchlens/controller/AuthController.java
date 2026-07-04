package com.zzx.matchlens.controller;

import com.zzx.matchlens.common.Result;
import com.zzx.matchlens.dto.LoginRequest;
import com.zzx.matchlens.dto.LoginResponse;
import com.zzx.matchlens.dto.RegisterRequest;
import com.zzx.matchlens.dto.UpdateNicknameRequest;
import com.zzx.matchlens.dto.UpdatePasswordRequest;
import com.zzx.matchlens.entity.User;
import com.zzx.matchlens.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.ok();
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.ok(response);
    }

    /**
     * 获取当前登录用户信息（需鉴权，拦截器会将 userId 写入 request attribute）
     */
    @GetMapping("/me")
    public Result<User> me(HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        User user = userService.getUserById(Long.parseLong(userIdStr));
        return Result.ok(user);
    }

    /**
     * 修改昵称
     */
    @PutMapping("/nickname")
    public Result<Void> updateNickname(@RequestBody UpdateNicknameRequest request,
                                       HttpServletRequest httpRequest) {
        String userIdStr = (String) httpRequest.getAttribute("userId");
        userService.updateNickname(Long.parseLong(userIdStr), request.getNickname());
        return Result.ok();
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody UpdatePasswordRequest request,
                                       HttpServletRequest httpRequest) {
        String userIdStr = (String) httpRequest.getAttribute("userId");
        userService.updatePassword(Long.parseLong(userIdStr), request.getOldPassword(), request.getNewPassword());
        return Result.ok();
    }
}
