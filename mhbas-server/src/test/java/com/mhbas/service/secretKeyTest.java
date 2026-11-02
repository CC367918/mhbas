package com.mhbas.service;

import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Base64;

@SpringBootTest
public class secretKeyTest {
    @Test
    void generateSecretKey() {
        // 生成安全随机的256位密钥（32字节）
        var secretKey = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String base64Key = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        System.out.println("Base64密钥：");
        System.out.println(base64Key);
    }
}
