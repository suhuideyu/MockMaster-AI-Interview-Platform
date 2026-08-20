package com.mockmaster.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrowthCurveDTO {
    private String date;
    private Integer interviewCount;
    private Integer totalDuration;
    private BigDecimal avgScore;
    private BigDecimal avgAccuracy;
    private BigDecimal avgProfessional;
    private BigDecimal avgLogic;
}
