package com.mhbas.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadAvatar(Long userId, MultipartFile file);
}
