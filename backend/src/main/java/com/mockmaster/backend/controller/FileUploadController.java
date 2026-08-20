package com.mockmaster.backend.controller;

import com.mockmaster.backend.common.ApiResponse;
import com.mockmaster.backend.dto.FileUploadResponse;
import com.mockmaster.backend.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * 文件上传接口
     * @param file 上传的文件，参数名必须为 file
     * @return 返回上传结果，包含可访问的 URL
     */
    @PostMapping("/upload")
    public ApiResponse<?> upload(@RequestParam("file") MultipartFile file) {
        log.info("接收到文件上传请求: {}", file.getOriginalFilename());
        
        FileUploadResponse response = fileUploadService.uploadFile(file);
        return ApiResponse.success("上传成功", response);
    }
}
