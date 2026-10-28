package com.mhbas.service.impl;

import com.mhbas.service.FileStorageService;

import com.mhbas.exception.BusinessException;
import com.mhbas.utils.AliOssUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {
    private static final long MAX_AVATAR_SIZE = 5L * 1024 * 1024;

    private final AliOssUtil aliOssUtil;

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    public FileStorageServiceImpl(AliOssUtil aliOssUtil) {
        this.aliOssUtil = aliOssUtil;
    }

    /**
     * 上传头像
     * @param userId
     * @param file
     * @return
     */
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择头像图片");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException("头像不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("仅支持 JPG、PNG、WEBP 或 GIF 图片");
        }

        String extension = extensionOf(file.getOriginalFilename(), contentType);
        String objectKey = "mhbas/avatars/" + userId + "/" + UUID.randomUUID() + extension;     // 构建对象存储的键
        try {
            String filePath= aliOssUtil.upload(file.getBytes(), objectKey, contentType);     // 上传文件到 OSS
            return filePath;
        } catch (Exception exception) {
            if (exception instanceof BusinessException businessException) {
                throw businessException;
            }
            log.error("头像上传失败, userId={}, objectKey={}, contentType={}, size={}",
                    userId, objectKey, contentType, file.getSize(), exception);
            throw new BusinessException(502, "头像上传失败，请检查 OSS 配置");
        }
    }
    // 获取文件扩展名
    private String extensionOf(String filename, String contentType) {
        if (StringUtils.hasText(filename) && filename.lastIndexOf('.') >= 0) {
            String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif").contains(extension)) {
                return extension;
            }
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }
}
