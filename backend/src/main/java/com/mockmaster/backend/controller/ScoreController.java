package com.mockmaster.backend.controller;

import com.mockmaster.backend.common.ApiResponse;
import com.mockmaster.backend.dto.GrowthCurveDTO;
import com.mockmaster.backend.dto.ScoreDetailDTO;
import com.mockmaster.backend.dto.ScoreOverviewDTO;
import com.mockmaster.backend.security.LoginUser;
import com.mockmaster.backend.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scores")
@RequiredArgsConstructor
public class ScoreController {
    
    private final ScoreService scoreService;
    
    @GetMapping("/overview")
    public ApiResponse<ScoreOverviewDTO> getScoreOverview(Authentication authentication) {
        Long userId = ((LoginUser) authentication.getPrincipal()).userId();
        ScoreOverviewDTO data = scoreService.getScoreOverview(userId);
        return ApiResponse.success(data);
    }
    
    @GetMapping("/growth-curve")
    public ApiResponse<List<GrowthCurveDTO>> getGrowthCurveData(Authentication authentication) {
        Long userId = ((LoginUser) authentication.getPrincipal()).userId();
        List<GrowthCurveDTO> data = scoreService.getGrowthCurveData(userId);
        return ApiResponse.success(data);
    }
    
    @GetMapping("/interviews/{interviewId}")
    public ApiResponse<ScoreDetailDTO> getInterviewScoreDetail(
            Authentication authentication,
            @PathVariable Long interviewId) {
        Long userId = ((LoginUser) authentication.getPrincipal()).userId();
        ScoreDetailDTO data = scoreService.getInterviewScoreDetail(userId, interviewId);
        return ApiResponse.success(data);
    }
}
