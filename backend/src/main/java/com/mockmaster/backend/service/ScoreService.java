package com.mockmaster.backend.service;

import com.mockmaster.backend.dto.ScoreDetailDTO;
import com.mockmaster.backend.dto.ScoreOverviewDTO;
import com.mockmaster.backend.dto.GrowthCurveDTO;

import java.util.List;

public interface ScoreService {
    ScoreOverviewDTO getScoreOverview(Long userId);

    List<GrowthCurveDTO> getGrowthCurveData(Long userId);
    
    ScoreDetailDTO getInterviewScoreDetail(Long userId, Long interviewId);
}
