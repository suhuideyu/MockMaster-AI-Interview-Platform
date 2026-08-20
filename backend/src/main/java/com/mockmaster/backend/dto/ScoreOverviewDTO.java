package com.mockmaster.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreOverviewDTO {
    private Integer completedCount;
    private BigDecimal avgAccuracy;
    private BigDecimal avgProfessional;
    private BigDecimal avgLogic;
    private BigDecimal avgTotalScore;
    
    private Integer voiceCount;
    private Integer textCount; 
    private Integer totalPlannedDuration;
    private Integer totalActualDuration;
    
    private List<InterviewSummary> recentSummaries;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InterviewSummary {
        private Long interviewId;
        private String jobName;
        private String summary;
        private BigDecimal totalScore;
    }
}
