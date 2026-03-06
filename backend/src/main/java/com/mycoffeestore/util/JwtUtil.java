package com.mycoffeestore.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成 Token（旧版，不含角色信息）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return Token
     * @deprecated 使用 {@link #generateToken(Long, String, String)} 代替
     */
    @Deprecated
    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, "user");
    }

    /**
     * 生成 Token（包含角色信息）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色编码
     * @return Token
     */
    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析 Token
     *
     * @param token Token
     * @return Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取用户ID
     *
     * @param token Token
     * @return 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 获取用户名
     *
     * @param token Token
     * @return 用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 获取角色编码
     *
     * @param token Token
     * @return 角色编码
     */
    public String getRole(String token) {
        Claims claims = parseToken(token);
        String role = claims.get("role", String.class);
        return role != null ? role : "user";
    }

    /**
     * 验证 Token 是否过期
     *
     * @param token Token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取密钥
     *
     * @return 密钥
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
