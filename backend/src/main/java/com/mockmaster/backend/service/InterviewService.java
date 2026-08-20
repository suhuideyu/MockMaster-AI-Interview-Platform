package com.mockmaster.backend.service;

import com.mockmaster.backend.dto.InterviewHistoryResponse;
import com.mockmaster.backend.dto.InterviewMessageRequest;
import com.mockmaster.backend.dto.InterviewMessageResponse;
import com.mockmaster.backend.dto.InterviewStartRequest;
import com.mockmaster.backend.dto.InterviewStartResponse;
import com.mockmaster.backend.dto.SubmitVoiceResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InterviewService {
    InterviewStartResponse startInterview(Long userId, InterviewStartRequest request);

    InterviewMessageResponse sendMessage(Long userId, Long interviewId, InterviewMessageRequest request);

    void completeInterview(Long userId, Long interviewId);

    void abortInterview(Long userId, Long interviewId);

    List<InterviewHistoryResponse> history(Long userId);

    void deleteInterview(Long userId, Long interviewId);

    /**
     * 提交语音回答并进行AI评分
     * @param userId 用户ID
     * @param interviewId 面试ID
     * @param questionId 题目ID
     * @param file 语音文件
     * @return 评分结果和下一题信息
     */
    SubmitVoiceResponse submitVoiceAnswer(Long userId, Long interviewId, Long questionId, MultipartFile file);

    /**
     * @param userId 用户ID
     * @param interviewId 面试ID
     * @param questionId 题目ID
     * @param textContent 用户输入的文本内容
     * @return 评分结果和下一题信息
     */
    SubmitVoiceResponse submitTextAnswer(Long userId, Long interviewId, Long questionId, String textContent);
}
