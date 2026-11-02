package com.mhbas.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * 生成jwt
     * @param secretKey
     * @param expirationMillis
     * @param claims
     * @return
     */
    public static String createJWT(String secretKey, Long expirationMillis, Map<String, Object> claims) {
        SecretKey key = buildKey(secretKey);     // 动态生成密钥
        Date expiration = new Date(System.currentTimeMillis() + expirationMillis);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(key)          // 与 parseJWT 使用同一密钥，算法由密钥长度决定，保证签验一致
                .compact();
    }

    /**
     * Token解密
     * @param secretKey jwt秘钥
     * @param token jwt令牌
     * @return
     */
    public static Claims parseJWT(String secretKey, String token) {
        SecretKey key = buildKey(secretKey);
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //统一构建密钥，并在配置缺失/过短时给出明确错误，避免运行期 NPE
    private static SecretKey buildKey(String secretKey) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("JWT 密钥未配置，请检查 jwt.secret-key 或环境变量 JWT_SECRET");
        }
        byte[] bytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {   // HS256 要求密钥不少于 32 字节
            throw new IllegalStateException("JWT 密钥长度不足 32 字节，请更换更长的密钥");
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    //从请求头原始值中提取令牌，兼容带/不带 Bearer 前缀两种写法
    public static String resolveToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        String token = authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())
                ? authorization.substring(BEARER_PREFIX.length())
                : authorization;
        token = token.trim();
        return token.isEmpty() ? null : token;
    }

}
