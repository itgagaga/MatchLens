package com.zzx.matchlens.config;

import com.zzx.matchlens.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        String token = null;
    
        // 优先从 Authorization 头获取 token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
    
        // 回退：从 query 参数获取 token（EventSource SSE 不支持自定义请求头）
        if (token == null || token.isBlank()) {
            token = request.getParameter("token");
        }
    
        if (token == null || token.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"\u672a\u767b\u5f55\u6216token\u5df2\u8fc7\u671f\"}");
            return false;
        }
    
        try {
            Claims claims = jwtUtil.parseToken(token);
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            // 将用户信息存入 request，供 Controller 使用
            request.setAttribute("userId", userId);
            request.setAttribute("role", role);

            // 检查是否需要 ADMIN 权限（通过 request attribute 传入标记）
            Boolean requireAdmin = (Boolean) request.getAttribute("requireAdmin");
            if (Boolean.TRUE.equals(requireAdmin) && !"ADMIN".equals(role)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"权限不足，需要管理员权限\"}");
                return false;
            }

            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"token无效或已过期\"}");
            return false;
        }
    }
}
