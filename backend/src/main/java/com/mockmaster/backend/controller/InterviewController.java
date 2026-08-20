package com.mockmaster.backend.controller;

import com.mockmaster.backend.common.ApiResponse;
import com.mockmaster.backend.dto.InterviewHistoryResponse;
import com.mockmaster.backend.dto.InterviewMessageRequest;
import com.mockmaster.backend.dto.InterviewMessageResponse;
import com.mockmaster.backend.dto.InterviewStartRequest;
import com.mockmaster.backend.dto.InterviewStartResponse;
import com.mockmaster.backend.dto.SubmitVoiceResponse;
import com.mockmaster.backend.security.LoginUser;
import com.mockmaster.backend.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/start")
    public ApiResponse<InterviewStartResponse> start(Authentication authentication,
                                                     @Valid @RequestBody InterviewStartRequest request) {
        Long userId = ((LoginUser) authentication.getPrincipal()).userId();
        return ApiResponse.success(interviewService.startInterview(userId, request));
    }

    @PostMapping("/{interviewId}/messages")
    public ApiResponse<InterviewMessageResponse> sendMessage(Authentication authentication,
                                                            @PathVariable Long interviewId,
                                                            @Valid @RequestBody InterviewMessageRequest request) {
        Long userId = ((LoginUser) authentication.getPrincipal()).userId();
        return ApiResponse.success(interviewService.sendMessage(userId, interviewId, request));
    }

    @PostMapping("/{interviewId}/complete")
    public ApiResponse<Void> complete(Authentication authentication, @PathVariable Long interviewId) {
        Long userId = ((LoginUser) authentication.getPrincipal()).userId();
        interviewService.completeInterview(userId, interviewId);
        return ApiResponse.success("面试已完成并保存", null);
    }

    @PostMapping("/{interviewId}/abort")
    public ApiResponse<Void> abort(Authentication authentication, @PathVariable Long interviewId) {
        Long userId = ((LoginUser) authentication.getPrincipal()).userId();
        interviewService.abortInterview(userId, interviewId);
        return ApiResponse.success("面试已终止", null);
    }

    @GetMapping("/history")
    public ApiResponse<List<InterviewHistoryResponse>> history(Authentication authentication) {
        Long userId = ((LoginUser) authentication.getPrincipal()).userId();
        return ApiResponse.success(interviewService.history(userId));
    }

    @DeleteMapping("/{interviewId}")
    public ApiResponse<Void> delete(Authentication authentication, @PathVariable Long interviewId) {
        Long userId = ((LoginUser) authentication.getPrincipal()).userId();
        interviewService.deleteInterview(userId, interviewId);
        return ApiResponse.success("删除成功", null);
    }

    @PostMapping("/{interviewId}/submitVoice")
    public ApiResponse<SubmitVoiceResponse> submitVoiceAnswer(Authentication authentication,
                                                              @PathVariable Long interviewId,
                                                              @RequestParam Long questionId,
                                                              @RequestParam("file") MultipartFile file) {
        Long userId = ((LoginUser) authentication.getPrincipal()).userId();
        SubmitVoiceResponse response = interviewService.submitVoiceAnswer(userId, interviewId, questionId, file);
        return ApiResponse.success(response);
    }

    @PostMapping("/{interviewId}/submitText")
    public ApiResponse<SubmitVoiceResponse> submitTextAnswer(Authentication authentication,
                                                             @PathVariable Long interviewId,
                                                             @RequestParam Long questionId,
                                                             @RequestParam String text) {
        Long userId = ((LoginUser) authentication.getPrincipal()).userId();
        SubmitVoiceResponse response = interviewService.submitTextAnswer(userId, interviewId, questionId, text);
        return ApiResponse.success(response);
    }
}
