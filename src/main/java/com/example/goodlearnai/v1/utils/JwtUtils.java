package com.example.goodlearnai.v1.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {
    // 使用普通字符串作为密钥（必须至少 32 字节）
    private static final String SECRET_KEY_STRING = "your-256-bit-secret-key-must-be-at-least-32-bytes";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());
    private static final long EXPIRATION_MS = 86400000L; // 1 天（单位：毫秒）

    /**
     * 生成 JWT 令牌
     */
    public static String generateToken(Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS)) // 过期时间
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) // 使用字符串密钥
                .compact();
    }

    /**
     * 解析 JWT 令牌，获取用户 ID
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 解析 JWT 令牌，获取用户角色
     */
    public static String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 校验 JWT 令牌是否有效
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 通用解析 JWT 令牌的方法
     */
    private static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY) // 使用字符串密钥进行解析
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}