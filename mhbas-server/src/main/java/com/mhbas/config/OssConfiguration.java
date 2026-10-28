package com.mhbas.config;

import com.mhbas.properties.AliOssProperties;
import com.mhbas.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class OssConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil(AliOssProperties properties) {
        log.info("Initializing Aliyun OSS client: endpoint={}, bucket={}",
                properties.getEndpoint(), properties.getBucketName());
        return new AliOssUtil(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret(),
                properties.getBucketName(),
                properties.getPublicBaseUrl()
        );
    }
}
