package com.mockmaster.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitVoiceResponse {
    private String nextTitle;
    private Long nextQuestionId;
    private String userText;
    private Double totalScore;
    private Integer roundCount;
}
