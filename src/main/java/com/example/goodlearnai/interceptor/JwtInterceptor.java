package com.example.goodlearnai.interceptor;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNullApi;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request,  HttpServletResponse response, Object handler) throws Exception {
        // 从请求头中获取 Token
        String token = request.getHeader("Authorization");

        // 如果 Token 为空，返回未授权错误
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, "用户鉴权错误")));
            return false;
        }

        // 验证 Token 是否有效
        if (!JwtUtils.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, "失效的token")));
            return false;
        }

        // 从 Token 中提取用户信息并存储到请求中
        Long userId = JwtUtils.getUserIdFromToken(token);
        String role = JwtUtils.getRoleFromToken(token);

        request.setAttribute("userId", userId);
        request.setAttribute("role", role);

        return true;
    }
}