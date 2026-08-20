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
public class ScoreDetailDTO {
    private Long interviewId;
    private String jobName;
    private String difficulty;
    private String mode;
    private Integer plannedDuration;
    private Integer actualDuration;
    private BigDecimal avgAccuracy;
    private BigDecimal avgProfessional;
    private BigDecimal avgLogic;
    private BigDecimal totalScore;
    
    private List<QuestionScore> questionScores;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionScore {
        private Long questionId;
        private String question;
        private String userText;
        private String answerMode;
        private BigDecimal accuracy;
        private BigDecimal professional;
        private BigDecimal logic;
        private BigDecimal totalScore;
    }
}
