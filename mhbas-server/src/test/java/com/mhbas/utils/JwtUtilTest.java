package com.mhbas.utils;

import com.mhbas.properties.JwtProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

//class JwtUtilTest {
//
//    @Test
//    void shouldGenerateAndParseJwtClaims() {
//        JwtProperties properties = new JwtProperties();
//        properties.setSecret("test-secret-for-mhbas-that-is-long-enough-2026");
//        properties.setExpiration(60_000);
//        JwtUtil jwtUtil = new JwtUtil(properties);
//
//        String token = jwtUtil.generateToken(42L, "demo");
//
//        assertEquals(42L, jwtUtil.parseUserId(token));
//        assertEquals("demo", jwtUtil.parseUsername(token));
//    }
//}
