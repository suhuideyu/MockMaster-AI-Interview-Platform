package com.mockmaster.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mockmaster.backend.common.BusinessException;
import com.mockmaster.backend.dto.InterviewHistoryResponse;
import com.mockmaster.backend.dto.InterviewMessageRequest;
import com.mockmaster.backend.dto.InterviewMessageResponse;
import com.mockmaster.backend.dto.InterviewStartRequest;
import com.mockmaster.backend.dto.InterviewStartResponse;
import com.mockmaster.backend.dto.PythonAiResponse;
import com.mockmaster.backend.dto.SubmitVoiceResponse;
import com.mockmaster.backend.entity.Interview;
import com.mockmaster.backend.entity.InterviewDetail;
import com.mockmaster.backend.entity.Job;
import com.mockmaster.backend.entity.Message;
import com.mockmaster.backend.entity.Resource;
import com.mockmaster.backend.mapper.InterviewDetailMapper;
import com.mockmaster.backend.mapper.InterviewMapper;
import com.mockmaster.backend.mapper.JobMapper;
import com.mockmaster.backend.mapper.MessageMapper;
import com.mockmaster.backend.mapper.ResourceMapper;
import com.mockmaster.backend.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewServiceImpl implements InterviewService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String PYTHON_AI_BASE_URL = "http://127.0.0.1:8000";

    private final InterviewMapper interviewMapper;
    private final MessageMapper messageMapper;
    private final JobMapper jobMapper;
    private final ResourceMapper resourceMapper;
    private final InterviewDetailMapper interviewDetailMapper;
    private final RestTemplate restTemplate;
    private final XfyunAvatarService xfyunAvatarService;

    @Value("${app.upload-dir:D:\\idea_workspace\\MockMaster\\uploads}")
    private String uploadDir;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterviewStartResponse startInterview(Long userId, InterviewStartRequest request) {
        Job job = jobMapper.selectById(request.getJobId());
        if (job == null) {
            throw new BusinessException("岗位不存在");
        }

        Interview interview = new Interview();
        interview.setUserId(userId);
        interview.setJobId(request.getJobId());
        interview.setDifficulty(request.getDifficulty());
        interview.setPlannedDuration(request.getDuration());
        interview.setActualDuration(0);
        interview.setMode(request.getMode());
        interview.setStatus("IN_PROGRESS");
        interview.setRoundCount(0);
        interview.setTotalScore(BigDecimal.ZERO);
        interview.setStartTime(LocalDateTime.now());
        interviewMapper.insert(interview);
        Resource firstQuestion = getNextQuestionFromDb(request.getJobId(), request.getDifficulty(), interview.getId());
        if (firstQuestion == null) {
            throw new BusinessException("该岗位无可用题目，请联系管理员");
        }
        
        String openingQuestion = firstQuestion.getTitle();
        saveMessage(interview.getId(), "AI", openingQuestion);
        interview.setLastQuestion(openingQuestion);
        interviewMapper.updateById(interview);
        
        if ("voice".equalsIgnoreCase(request.getMode())) {
            driveVirtualHumanAsync(openingQuestion);
        }
        
        return new InterviewStartResponse(interview.getId(), openingQuestion, firstQuestion.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterviewMessageResponse sendMessage(Long userId, Long interviewId, InterviewMessageRequest request) {
        Interview interview = requireInterview(userId, interviewId);
        if (!"IN_PROGRESS".equals(interview.getStatus())) {
            throw new BusinessException("当前面试已结束");
        }

        saveMessage(interviewId, "USER", request.getContent());
        int nextRound = interview.getRoundCount() + 1;
        interview.setRoundCount(nextRound);
        Resource nextQuestion = getNextQuestionFromDb(interview.getJobId(), interview.getDifficulty(), interviewId);
        String reply;
        
        if (nextQuestion != null) {
            reply = nextQuestion.getTitle();
            interview.setLastQuestion(reply);
            driveVirtualHumanAsync(reply);
        } else {
            reply = "面试题库已用尽，本轮面试结束。感谢你的参与！";
            interview.setStatus("COMPLETED");
            interview.setEndTime(LocalDateTime.now());
        }
        
        saveMessage(interviewId, "AI", reply);
        interviewMapper.updateById(interview);
        return new InterviewMessageResponse(reply, nextRound);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeInterview(Long userId, Long interviewId) {
        Interview interview = requireInterview(userId, interviewId);
        if (!"IN_PROGRESS".equals(interview.getStatus())) {
            throw new BusinessException("面试状态不允许完成");
        }

        LocalDateTime now = LocalDateTime.now();
        long actualMinutes = Math.max(1, Duration.between(interview.getStartTime(), now).toMinutes());
        interview.setEndTime(now);
        interview.setActualDuration((int) actualMinutes);
        interview.setStatus("COMPLETED");
        interview.setSummary(buildSummary(interview));
        interview.setTotalScore(BigDecimal.valueOf(calculateScore(interview)));
        interviewMapper.updateById(interview);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void abortInterview(Long userId, Long interviewId) {
        Interview interview = requireInterview(userId, interviewId);
        if (!"IN_PROGRESS".equals(interview.getStatus())) {
            return;
        }

        interview.setStatus("ABORTED");
        interview.setEndTime(LocalDateTime.now());
        interviewMapper.updateById(interview);
    }

    @Override
    public List<InterviewHistoryResponse> history(Long userId) {
        List<Interview> interviews = interviewMapper.selectList(new LambdaQueryWrapper<Interview>()
                .eq(Interview::getUserId, userId)
                .eq(Interview::getStatus, "COMPLETED")
                .orderByDesc(Interview::getEndTime));

        return interviews.stream().map(item -> {
            Job job = jobMapper.selectById(item.getJobId());
            List<InterviewDetail> details = interviewDetailMapper.selectList(new LambdaQueryWrapper<InterviewDetail>()
                    .eq(InterviewDetail::getInterviewId, item.getId()));
            BigDecimal averageScore = calculateAverageScore(details);
            
            return InterviewHistoryResponse.builder()
                    .id(item.getId())
                    .jobId(item.getJobId())
                    .jobName(job != null ? job.getJobName() : "未知岗位")
                    .difficulty(item.getDifficulty())
                    .plannedDuration(item.getPlannedDuration())
                    .actualDuration(item.getActualDuration())
                    .mode(item.getMode())
                    .status(item.getStatus())
                    .summary(item.getSummary())
                    .lastQuestion(item.getLastQuestion())
                    .endTime(item.getEndTime() == null ? "" : item.getEndTime().format(FORMATTER))
                    .averageScore(averageScore)
                    .build();
        }).toList();
    }
    private BigDecimal calculateAverageScore(List<InterviewDetail> details) {
        if (details == null || details.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal sum = details.stream()
                .map(InterviewDetail::getTotalScore)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int count = (int) details.stream().map(InterviewDetail::getTotalScore).filter(v -> v != null).count();
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        
        return sum.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInterview(Long userId, Long interviewId) {
        requireInterview(userId, interviewId);
        messageMapper.delete(new LambdaQueryWrapper<Message>().eq(Message::getInterviewId, interviewId));
        interviewDetailMapper.delete(new LambdaQueryWrapper<InterviewDetail>().eq(InterviewDetail::getInterviewId, interviewId));
        interviewMapper.deleteById(interviewId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmitVoiceResponse submitVoiceAnswer(Long userId, Long interviewId, Long questionId, MultipartFile file) {
        log.info("Processing voice answer: user={}, interview={}, question={}", userId, interviewId, questionId);
        
        Interview interview = requireInterview(userId, interviewId);
        if (!"IN_PROGRESS".equals(interview.getStatus())) {
            throw new BusinessException("当前面试已结束");
        }
        
        Resource question = resourceMapper.selectById(questionId);
        if (question == null || !"question".equals(question.getResourceType())) {
            throw new BusinessException("题目不存在");
        }
        
        String audioFilename = String.format("%d_%d.wav", interviewId, questionId);
        String audioPath = saveAudioFile(file, audioFilename);
        
        try {
            PythonAiResponse aiResponse = analyzeResponse(interviewId, questionId, "voice", file, null);
            
            InterviewDetail detail = new InterviewDetail();
            detail.setInterviewId(interviewId);
            detail.setQuestionId(questionId);
            detail.setUserText(aiResponse.getUserText());
            detail.setScoreAccuracy(aiResponse.getScoreAccuracy());
            detail.setScoreProfessional(aiResponse.getScoreProfessional());
            detail.setScoreLogic(aiResponse.getScoreLogic());
            detail.setTotalScore(aiResponse.getTotalScore());
            detail.setAudioUrl(audioPath);
            detail.setAnswerMode("voice");
            interviewDetailMapper.insert(detail);
            log.info("Saved interview detail: id={}", detail.getId());
            
            interview.setRoundCount(interview.getRoundCount() + 1);
            BigDecimal currentAvgScore = interview.getTotalScore()
                    .multiply(BigDecimal.valueOf(interview.getRoundCount() - 1))
                    .add(aiResponse.getTotalScore())
                    .divide(BigDecimal.valueOf(interview.getRoundCount()), 2, java.math.RoundingMode.HALF_UP);
            interview.setTotalScore(currentAvgScore);
            interviewMapper.updateById(interview);
            Resource nextQuestion = getNextQuestionFromDb(interview.getJobId(), interview.getDifficulty(), interviewId);
            
            SubmitVoiceResponse response = new SubmitVoiceResponse();
            response.setUserText(aiResponse.getUserText());
            response.setTotalScore(aiResponse.getTotalScore().doubleValue());
            response.setRoundCount(interview.getRoundCount());
            
            if (nextQuestion != null) {
                response.setNextQuestionId(nextQuestion.getId());
                response.setNextTitle(nextQuestion.getTitle());
                interview.setLastQuestion(nextQuestion.getTitle());
                interviewMapper.updateById(interview);
                driveVirtualHumanAsync(nextQuestion.getTitle());
            } else {
                response.setNextTitle("面试题库已用尽，本轮面试结束");
                response.setNextQuestionId(null);
                interview.setStatus("COMPLETED");
                interview.setEndTime(LocalDateTime.now());
                interviewMapper.updateById(interview);
            }
            
            log.info("Voice answer processed successfully: response={}", response);
            return response;
            
        } catch (Exception e) {
            log.error("Error processing voice answer", e);
            if (audioPath != null) {
                try {
                    Files.deleteIfExists(Paths.get(audioPath));
                } catch (IOException ex) {
                    log.warn("Failed to delete audio file: {}", audioPath, ex);
                }
            }
            throw new BusinessException("语音分析失败: " + e.getMessage());
        }
    }

    private String saveAudioFile(MultipartFile file, String filename) {
        try {
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                boolean created = uploadDirectory.mkdirs();
                if (!created) {
                    throw new IOException("无法创建上传目录: " + uploadDir);
                }
            }
            
            Path filePath = Paths.get(uploadDir, filename);
            Files.write(filePath, file.getBytes());
            log.info("Audio file saved: {}", filePath);
            return filePath.toString();
            
        } catch (IOException e) {
            log.error("Failed to save audio file", e);
            throw new BusinessException("保存音频文件失败: " + e.getMessage());
        }
    }

    /**
     * 用于文本模式下用户提交回答
     * 
     * @param userId 用户ID
     * @param interviewId 面试ID
     * @param questionId 题目ID
     * @param textContent 用户输入的文本内容
     * @return 评分结果和下一题信息
     */
    @Transactional(rollbackFor = Exception.class)
    public SubmitVoiceResponse submitTextAnswer(Long userId, Long interviewId, Long questionId, String textContent) {
        log.info("Processing text answer: user={}, interview={}, question={}, text length={}", 
                 userId, interviewId, questionId, textContent != null ? textContent.length() : 0);
        
        Interview interview = requireInterview(userId, interviewId);
        if (!"IN_PROGRESS".equals(interview.getStatus())) {
            throw new BusinessException("当前面试已结束");
        }
        
        Resource question = resourceMapper.selectById(questionId);
        if (question == null || !"question".equals(question.getResourceType())) {
            throw new BusinessException("题目不存在");
        }
        
        if (textContent == null || textContent.trim().isEmpty()) {
            throw new BusinessException("回答内容不能为空");
        }
        
        try {
            PythonAiResponse aiResponse = analyzeResponse(interviewId, questionId, "text", null, textContent);
            InterviewDetail detail = new InterviewDetail();
            detail.setInterviewId(interviewId);
            detail.setQuestionId(questionId);
            detail.setUserText(aiResponse.getUserText());
            detail.setScoreAccuracy(aiResponse.getScoreAccuracy());
            detail.setScoreProfessional(aiResponse.getScoreProfessional());
            detail.setScoreLogic(aiResponse.getScoreLogic());
            detail.setTotalScore(aiResponse.getTotalScore());
            detail.setAnswerMode("text");
            interviewDetailMapper.insert(detail);
            log.info("Saved interview detail: id={}", detail.getId());
            interview.setRoundCount(interview.getRoundCount() + 1);
            BigDecimal currentAvgScore = interview.getTotalScore()
                    .multiply(BigDecimal.valueOf(interview.getRoundCount() - 1))
                    .add(aiResponse.getTotalScore())
                    .divide(BigDecimal.valueOf(interview.getRoundCount()), 2, java.math.RoundingMode.HALF_UP);
            interview.setTotalScore(currentAvgScore);
            interviewMapper.updateById(interview);
            Resource nextQuestion = getNextQuestionFromDb(interview.getJobId(), interview.getDifficulty(), interviewId);
            
            SubmitVoiceResponse response = new SubmitVoiceResponse();
            response.setUserText(aiResponse.getUserText());
            response.setTotalScore(aiResponse.getTotalScore().doubleValue());
            response.setRoundCount(interview.getRoundCount());
            
            if (nextQuestion != null) {
                response.setNextQuestionId(nextQuestion.getId());
                response.setNextTitle(nextQuestion.getTitle());
                interview.setLastQuestion(nextQuestion.getTitle());
                interviewMapper.updateById(interview);
                driveVirtualHumanAsync(nextQuestion.getTitle());
            } else {
                response.setNextTitle("面试题库已用尽，本轮面试结束");
                response.setNextQuestionId(null);
                interview.setStatus("COMPLETED");
                interview.setEndTime(LocalDateTime.now());
                interviewMapper.updateById(interview);
            }
            
            log.info("Text answer processed successfully: response={}", response);
            return response;
            
        } catch (Exception e) {
            log.error("Error processing text answer", e);
            throw new BusinessException("文本分析失败: " + e.getMessage());
        }
    }

    private PythonAiResponse callPythonAiService(MultipartFile file, String standardContent) {
        try {
            String url = PYTHON_AI_BASE_URL + "/analyze";
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("mode", "voice");
            body.add("standard", standardContent);
            
            body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            log.info("Calling Python AI service: {}", url);
            PythonAiResponse response = restTemplate.postForObject(url, requestEntity, PythonAiResponse.class);
            
            if (response == null) {
                throw new BusinessException("Python AI服务返回null");
            }
            
            log.info("Python AI response: {}", response);
            return response;
            
        } catch (Exception e) {
            log.error("Error calling Python AI service", e);
            throw new BusinessException("调用AI分析服务失败: " + e.getMessage());
        }
    }

    /**
     * @param interviewId 面试ID
     * @param questionId 题目ID
     * @param mode 模式：voice（语音）或 text（文本）
     * @param voiceFile 语音文件（mode=voice时）
     * @param textContent 文本内容（mode=text时）
     * @return 包含所有分数信息的响应对象
     */
    public PythonAiResponse analyzeResponse(Long interviewId, Long questionId, String mode, 
                                             MultipartFile voiceFile, String textContent) {
        try {
            log.info("Analyzing response: interviewId={}, questionId={}, mode={}", interviewId, questionId, mode);
            Resource question = resourceMapper.selectById(questionId);
            if (question == null || !"question".equals(question.getResourceType())) {
                throw new BusinessException("题目不存在");
            }
            
            String standardAnswer = question.getContent();
            PythonAiResponse aiResponse;
            if ("voice".equalsIgnoreCase(mode)) {
                aiResponse = callPythonAiServiceWithMode(voiceFile, standardAnswer, "voice");
            } else if ("text".equalsIgnoreCase(mode)) {
                aiResponse = callPythonAiServiceWithMode(null, standardAnswer, "text", textContent);
            } else {
                throw new BusinessException("不支持的模式: " + mode);
            }
            
            log.info("Analysis completed, saving to interview_detail: {}", aiResponse);
            return aiResponse;
            
        } catch (Exception e) {
            log.error("Error in analyzeResponse", e);
            throw new BusinessException("分析回答失败: " + e.getMessage());
        }
    }

    /**
     * 支持语音和文本两种模式
     * @param file 语音文件（仅语音模式使用）
     * @param standardContent 标准答案
     * @param mode 模式：voice 或 text
     * @param textContent 文本内容（仅文本模式使用）
     * @return Python 服务的响应结果
     */
    private PythonAiResponse callPythonAiServiceWithMode(MultipartFile file, String standardContent, 
                                                         String mode, String... textContent) {
        try {
            String url = PYTHON_AI_BASE_URL + "/analyze";
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("mode", mode);
            body.add("standard", standardContent);
            
            if ("voice".equalsIgnoreCase(mode)) {
                if (file == null) {
                    throw new BusinessException("语音模式必须提供音频文件");
                }
                body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                });
            } else if ("text".equalsIgnoreCase(mode)) {
                if (textContent == null || textContent.length == 0 || textContent[0] == null) {
                    throw new BusinessException("文本模式必须提供文本内容");
                }
                body.add("text", textContent[0]);
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            log.info("Calling Python AI service: {} with mode={}", url, mode);
            PythonAiResponse response = restTemplate.postForObject(url, requestEntity, PythonAiResponse.class);
            
            if (response == null) {
                throw new BusinessException("Python AI服务返回null");
            }
            
            log.info("Python AI response: {}", response);
            return response;
            
        } catch (Exception e) {
            log.error("Error calling Python AI service with mode: {}", mode, e);
            throw new BusinessException("调用AI分析服务失败: " + e.getMessage());
        }
    }

    private Interview requireInterview(Long userId, Long interviewId) {
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null || !userId.equals(interview.getUserId())) {
            throw new BusinessException("面试记录不存在");
        }
        return interview;
    }

    private void saveMessage(Long interviewId, String senderType, String content) {
        Message message = new Message();
        message.setInterviewId(interviewId);
        message.setSenderType(senderType);
        message.setContent(content);
        message.setSendTime(LocalDateTime.now());
        messageMapper.insert(message);
    }

    private String buildSummary(Interview interview) {
        String difficultyDesc = switch (interview.getDifficulty()) {
            case "easy" -> "基础交流较顺畅";
            case "hard" -> "在高压追问下仍保持了较好的作答完整度";
            default -> "整体回答结构较完整";
        };
        return difficultyDesc + "，建议继续补充量化结果、复杂场景处理和项目复盘表达。";
    }

    private double calculateScore(Interview interview) {
        int base = switch (interview.getDifficulty()) {
            case "easy" -> 78;
            case "hard" -> 84;
            default -> 81;
        };
        int roundBonus = Math.min(interview.getRoundCount() * 2, 10);
        int randomOffset = ThreadLocalRandom.current().nextInt(0, 6);
        return Math.min(98, base + roundBonus + randomOffset);
    }
    
    /**
     * @param jobId 岗位ID
     * @param difficultyStr 难度字符串 (easy/medium/hard)
     * @param interviewId 面试ID
     * @return 下一题，如果题库已用尽返回null
     */
    private Resource getNextQuestionFromDb(Long jobId, String difficultyStr, Long interviewId) {
        Integer difficulty = convertDifficulty(difficultyStr);
        
        log.info("Fetching next question for jobId={}, difficulty={}, interviewId={}", 
                jobId, difficulty, interviewId);
        
        Resource nextQuestion = resourceMapper.getRandomUnusedQuestion(jobId, difficulty, interviewId);
        
        if (nextQuestion != null) {
            log.info("Found next question: id={}, title={}", nextQuestion.getId(), nextQuestion.getTitle());
        } else {
            log.info("No more questions available for this interview");
        }
        
        return nextQuestion;
    }
    
    /**
     * 难度字符串转换为整数
     * easy -> 1, medium -> 2, hard -> 3
     * 
     * @param difficultyStr 难度字符串
     * @return 难度整数 (1/2/3)
     */
    private Integer convertDifficulty(String difficultyStr) {
        return switch (difficultyStr.toLowerCase(Locale.ROOT)) {
            case "easy" -> 1;
            case "hard" -> 3;
            default -> 2;
        };
    }
    
    /**
     * 异步驱动虚拟人播报题目
     * 不能因为虚拟人操作失败而导致面试流程中断
     * 
     * @param text 要播报的题目文本
     */
    private void driveVirtualHumanAsync(String text) {
        new Thread(() -> {
            try {
                if (text == null || text.trim().isEmpty()) {
                    return;
                }
                if (!xfyunAvatarService.isConnected()) {
                    log.warn("虚拟人未连接，无法播报题目");
                    return;
                }
                
                log.info("驱动虚拟人播报题目：{}", text);
                xfyunAvatarService.sendTextDriver(text);
                log.info("虚拟人播报完成");
            } catch (Exception e) {
                log.warn("驱动虚拟人失败（不影响面试流程）：{}", e.getMessage());
            }
        }).start();
    }
}