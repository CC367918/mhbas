package com.mhbas.properties;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliOssProperties {
    private String endpoint;   // OSS服务地址
    @ToString.Exclude
    private String accessKeyId;   // 访问密钥ID
    @ToString.Exclude
    private String accessKeySecret;   // 访问密钥密钥
    private String bucketName;
    private String publicBaseUrl;   // 公共访问域名
}
