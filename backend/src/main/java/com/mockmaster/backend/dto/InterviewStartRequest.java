package com.mockmaster.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InterviewStartRequest {
    @NotNull(message = "岗位不能为空")
    private Long jobId;

    @NotBlank(message = "难度不能为空")
    private String difficulty;

    @NotNull(message = "时长不能为空")
    private Integer duration;

    @NotBlank(message = "模式不能为空")
    private String mode;
}
