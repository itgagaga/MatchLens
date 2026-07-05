package com.zzx.matchlens.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    public WebMvcConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 管理员拦截器：所有 /api/** 接口（排除 auth 相关和客户端接口），需要 ADMIN 角色
        registry.addInterceptor(new AdminInterceptor(jwtInterceptor))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/me",
                        "/api/auth/nickname",
                        "/api/auth/password",
                        "/api/client/**",
                        "/api/rankings/**"
                );

        // 客户端接口拦截器：/api/client/** 及排行榜等共享接口，需要登录（任意角色）
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/client/**", "/api/auth/me", "/api/auth/nickname", "/api/auth/password", "/api/rankings/**");
    }

    /**
     * 管理员拦截器：在 JWT 校验基础上额外要求 ADMIN 角色
     */
    private static class AdminInterceptor implements HandlerInterceptor {

        private final JwtInterceptor jwtInterceptor;

        AdminInterceptor(JwtInterceptor jwtInterceptor) {
            this.jwtInterceptor = jwtInterceptor;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            // 标记需要 ADMIN 权限
            request.setAttribute("requireAdmin", true);
            // 复用 JWT 拦截器的校验逻辑
            return jwtInterceptor.preHandle(request, response, handler);
        }
    }
}
