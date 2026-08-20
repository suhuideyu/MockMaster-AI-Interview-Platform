package com.mockmaster.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InterviewMessageRequest {
    @NotBlank(message = "回答内容不能为空")
    private String content;

    @NotBlank(message = "模式不能为空")
    private String mode;
}
