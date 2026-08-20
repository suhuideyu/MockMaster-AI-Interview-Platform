package com.mockmaster.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InterviewMessageResponse {
    private String reply;
    private Integer roundCount;
}
