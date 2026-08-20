package com.mockmaster.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InterviewStartResponse {
    private Long interviewId;
    private String openingQuestion;
    private Long questionId;
}
