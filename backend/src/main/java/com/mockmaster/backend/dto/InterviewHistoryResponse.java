package com.mockmaster.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class InterviewHistoryResponse {
    private Long id;
    private Long jobId;
    private String jobName;
    private String difficulty;
    private Integer plannedDuration;
    private Integer actualDuration;
    private String mode;
    private String status;
    private String summary;
    private String lastQuestion;
    private String endTime;
    private BigDecimal averageScore;
}
