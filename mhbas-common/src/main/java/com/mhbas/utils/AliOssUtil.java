package com.mhbas.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.mhbas.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Date;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {
    // 签名 URL 有效期6小时
    private static final long SIGNED_URL_TTL_MILLIS = 6L * 60 * 60 * 1000;  //

    private String endpoint;
    @ToString.Exclude
    private String accessKeyId;
    @ToString.Exclude
    private String accessKeySecret;
    private String bucketName;
    private String publicBaseUrl;

    /**
     * 文件上传
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName, String contentType) {
        // 创建OSSClient实例
        OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 创建PutObject请求
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);       // 声明 MIME 类型，如 image/png
            metadata.setContentLength(bytes.length);       // 声明内容长度
            client.putObject(bucketName, objectName, new ByteArrayInputStream(bytes), metadata);
        } catch (OSSException oe) {
            log.error("OSS 拒绝上传: code={}, message={}, requestId={}, hostId={}",
                    oe.getErrorCode(), oe.getErrorMessage(), oe.getRequestId(), oe.getHostId(), oe);
            throw new BusinessException(502, "上传到 OSS 失败：" + oe.getErrorCode());
        } catch (ClientException ce) {
            log.error("OSS 客户端异常: {}", ce.getMessage(), ce);
            throw new BusinessException(502, "无法连接 OSS，请检查 endpoint 与密钥配置");
        }
        finally {
            if(client != null){
                client.shutdown();
            }
        }
        //生成文件 URL
        String url = null;
        if (hasText(publicBaseUrl)) {
            url =publicBaseUrl.replaceAll("/+$", "") + "/" + objectName;
        }else{
            String host = endpoint.replaceFirst("^https?://", "").replaceAll("/+$", "");
            url ="https://" + bucketName + "." + host + "/" + objectName;
        }
        log.info("文件上传到:{}", url);
        return url;
    }

    /**
     * 把数据库里存的头像地址，转换成一个带临时访问授权的签名 URL
     * @param avatarUrl
     * @return
     */
    public String createSignedUrl(String avatarUrl) {
        String objectName = objectNameOf(avatarUrl);
        if (!isConfigured() || objectName == null) {
            return avatarUrl;
        }
        OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            Date expiration = new Date(System.currentTimeMillis() + SIGNED_URL_TTL_MILLIS);
            return client.generatePresignedUrl(bucketName, objectName, expiration).toString();  //生成带签名的临时URL
        } finally {
            //释放客户端
            client.shutdown();
        }
    }

    //从 URL 中反解出 OSS 对象键
    private String objectNameOf(String avatarUrl) {
        if (!hasText(avatarUrl)) {
            return null;
        }
        if (!avatarUrl.startsWith("http://") && !avatarUrl.startsWith("https://")) {
            return avatarUrl.replaceFirst("^/+", "");
        }
        try {
            String path = URI.create(avatarUrl).getPath();
            return hasText(path) ? path.replaceFirst("^/+", "") : null;
        } catch (IllegalArgumentException exception) {
            log.warn("Invalid avatar URL in database");
            return null;
        }
    }

    //判断字符串是否有效
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public boolean isConfigured() {
        return hasText(endpoint) && hasText(accessKeyId) && hasText(accessKeySecret) && hasText(bucketName);
    }
}
