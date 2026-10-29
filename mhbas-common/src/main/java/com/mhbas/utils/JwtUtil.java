package com.mhbas.utils;

import com.mhbas.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    /**
     * 生成jwt
     * @param secretKey
     * @param expirationMillis
     * @param claims
     * @return
     */
    public static String createJWT(String secretKey, Long expirationMillis, Map<String, Object> claims) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));     // 动态生成密钥
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
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
