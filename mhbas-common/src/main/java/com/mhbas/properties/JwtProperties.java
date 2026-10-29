package com.mhbas.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secretKey;      // 密钥
    private long expiration;    // 过期时间
    private String tokenName;   // 令牌名称
}
