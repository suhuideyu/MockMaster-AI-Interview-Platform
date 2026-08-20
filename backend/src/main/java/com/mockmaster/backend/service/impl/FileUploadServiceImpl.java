package com.mockmaster.backend.service.impl;

import com.mockmaster.backend.common.BusinessException;
import com.mockmaster.backend.dto.FileUploadResponse;
import com.mockmaster.backend.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${mockmaster.upload-dir:uploads}")
    private String uploadDir;

    @Value("${mockmaster.upload-url-prefix:/upload}")
    private String uploadUrlPrefix;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png"};

    @Override
    public FileUploadResponse uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过 5MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException("文件名不能为空");
        }

        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        if (!isAllowedExtension(fileExtension)) {
            throw new BusinessException("只支持 jpg、jpeg、png 格式的图片");
        }

        try {
            String newFilename = UUID.randomUUID() + "." + fileExtension;

            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                boolean created = uploadDirectory.mkdirs();
                if (!created) {
                    log.error("创建上传目录失败: {}", uploadDir);
                    throw new BusinessException("创建上传目录失败");
                }
            }
            File targetFile = new File(uploadDirectory, newFilename);
            file.transferTo(targetFile);

            log.info("文件上传成功: {}", newFilename);

            String accessUrl = uploadUrlPrefix + "/" + newFilename;
            return new FileUploadResponse(accessUrl, newFilename, file.getSize());

        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException("文件保存失败: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }

    private boolean isAllowedExtension(String extension) {
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}
