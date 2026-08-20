package com.mockmaster.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUploadResponse {
    private String url;
    private String filename;
    private Long size;
}
