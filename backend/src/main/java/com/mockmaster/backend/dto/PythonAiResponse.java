package com.mockmaster.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PythonAiResponse {
    @JsonProperty("user_text")
    private String userText;
    
    @JsonProperty("score_accuracy")
    private BigDecimal scoreAccuracy;
    
    @JsonProperty("score_professional")
    private BigDecimal scoreProfessional;
    
    @JsonProperty("score_logic")
    private BigDecimal scoreLogic;
    
    @JsonProperty("total_score")
    private BigDecimal totalScore;
}
