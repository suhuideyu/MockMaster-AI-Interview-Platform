package com.mockmaster.backend.service;

import com.mockmaster.backend.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    /**
     * 上传文件
     * @param file 上传的文件
     * @return 文件上传响应信息（包含可访问的URL）
     */
    FileUploadResponse uploadFile(MultipartFile file);
}
