package com.mockmaster.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceAnalysisResponse {
    private String userText;
    private BigDecimal scoreAccuracy;
    private BigDecimal scoreProfessional;
    private BigDecimal scoreLogic;
    private BigDecimal totalScore;
}
