package com.mockmaster.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mockmaster.backend.dto.GrowthCurveDTO;
import com.mockmaster.backend.dto.ScoreDetailDTO;
import com.mockmaster.backend.dto.ScoreOverviewDTO;
import com.mockmaster.backend.entity.Interview;
import com.mockmaster.backend.entity.InterviewDetail;
import com.mockmaster.backend.entity.Job;
import com.mockmaster.backend.entity.Resource;
import com.mockmaster.backend.mapper.InterviewDetailMapper;
import com.mockmaster.backend.mapper.InterviewMapper;
import com.mockmaster.backend.mapper.JobMapper;
import com.mockmaster.backend.mapper.ResourceMapper;
import com.mockmaster.backend.service.ScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreServiceImpl implements ScoreService {
    
    private final InterviewMapper interviewMapper;
    private final InterviewDetailMapper interviewDetailMapper;
    private final JobMapper jobMapper;
    private final ResourceMapper resourceMapper;
    
    @Override
    public ScoreOverviewDTO getScoreOverview(Long userId) {
        log.info("Getting score overview for user {}", userId);
        List<Interview> interviews = interviewMapper.selectList(new LambdaQueryWrapper<Interview>()
                .eq(Interview::getUserId, userId)
                .eq(Interview::getStatus, "COMPLETED"));
        
        int completedCount = interviews.size();
        
        if (completedCount == 0) {
            return ScoreOverviewDTO.builder()
                    .completedCount(0)
                    .avgAccuracy(BigDecimal.ZERO)
                    .avgProfessional(BigDecimal.ZERO)
                    .avgLogic(BigDecimal.ZERO)
                    .avgTotalScore(BigDecimal.ZERO)
                    .voiceCount(0)
                    .textCount(0)
                    .totalPlannedDuration(0)
                    .totalActualDuration(0)
                    .recentSummaries(new ArrayList<>())
                    .build();
        }

        int voiceCount = (int) interviews.stream().filter(i -> "voice".equalsIgnoreCase(i.getMode())).count();
        int textCount = completedCount - voiceCount;
        int totalPlanned = interviews.stream().mapToInt(i -> i.getPlannedDuration() != null ? i.getPlannedDuration() : 0).sum();
        int totalActual = interviews.stream().mapToInt(i -> i.getActualDuration() != null ? i.getActualDuration() : 0).sum();

        List<InterviewDetail> details = interviewDetailMapper.selectList(new LambdaQueryWrapper<InterviewDetail>()
                .in(InterviewDetail::getInterviewId, interviews.stream().map(Interview::getId).collect(Collectors.toList())));
        
        BigDecimal avgAccuracy = calculateAverage(details, d -> d.getScoreAccuracy());
        BigDecimal avgProfessional = calculateAverage(details, d -> d.getScoreProfessional());
        BigDecimal avgLogic = calculateAverage(details, d -> d.getScoreLogic());
        BigDecimal avgTotalScore = calculateAverage(details, d -> d.getTotalScore());

        List<ScoreOverviewDTO.InterviewSummary> recentSummaries = interviews.stream()
                .sorted((a, b) -> b.getEndTime().compareTo(a.getEndTime()))
                .limit(5)
                .map(interview -> {
                    Job job = jobMapper.selectById(interview.getJobId());
                    BigDecimal score = calculateInterviewAverageScore(interview.getId());
                    return ScoreOverviewDTO.InterviewSummary.builder()
                            .interviewId(interview.getId())
                            .jobName(job != null ? job.getJobName() : "未知岗位")
                            .summary(interview.getSummary())
                            .totalScore(score)
                            .build();
                })
                .collect(Collectors.toList());
        
        return ScoreOverviewDTO.builder()
                .completedCount(completedCount)
                .avgAccuracy(avgAccuracy)
                .avgProfessional(avgProfessional)
                .avgLogic(avgLogic)
                .avgTotalScore(avgTotalScore)
                .voiceCount(voiceCount)
                .textCount(textCount)
                .totalPlannedDuration(totalPlanned)
                .totalActualDuration(totalActual)
                .recentSummaries(recentSummaries)
                .build();
    }
    
    @Override
    public List<GrowthCurveDTO> getGrowthCurveData(Long userId) {
        log.info("Getting growth curve data for user {}", userId);
        
        List<Interview> interviews = interviewMapper.selectList(new LambdaQueryWrapper<Interview>()
                .eq(Interview::getUserId, userId)
                .eq(Interview::getStatus, "COMPLETED")
                .isNotNull(Interview::getEndTime));

        Map<LocalDate, List<Interview>> groupedByDate = interviews.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getEndTime().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<GrowthCurveDTO> result = new ArrayList<>();
        
        for (Map.Entry<LocalDate, List<Interview>> entry : groupedByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<Interview> dayInterviews = entry.getValue();
            
            int count = dayInterviews.size();
            int totalDuration = dayInterviews.stream()
                    .mapToInt(i -> i.getActualDuration() != null ? i.getActualDuration() : 0)
                    .sum();

            List<InterviewDetail> dayDetails = interviewDetailMapper.selectList(new LambdaQueryWrapper<InterviewDetail>()
                    .in(InterviewDetail::getInterviewId, dayInterviews.stream().map(Interview::getId).collect(Collectors.toList())));
            
            BigDecimal avgScore = calculateAverage(dayDetails, d -> d.getTotalScore());
            BigDecimal avgAccuracy = calculateAverage(dayDetails, d -> d.getScoreAccuracy());
            BigDecimal avgProfessional = calculateAverage(dayDetails, d -> d.getScoreProfessional());
            BigDecimal avgLogic = calculateAverage(dayDetails, d -> d.getScoreLogic());
            
            result.add(GrowthCurveDTO.builder()
                    .date(date.format(formatter))
                    .interviewCount(count)
                    .totalDuration(totalDuration)
                    .avgScore(avgScore)
                    .avgAccuracy(avgAccuracy)
                    .avgProfessional(avgProfessional)
                    .avgLogic(avgLogic)
                    .build());
        }
        
        return result;
    }
    
    @Override
    public ScoreDetailDTO getInterviewScoreDetail(Long userId, Long interviewId) {
        log.info("Getting score detail for user {} interview {}", userId, interviewId);
        
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null || !userId.equals(interview.getUserId())) {
            return null;
        }
        
        Job job = jobMapper.selectById(interview.getJobId());
        List<InterviewDetail> details = interviewDetailMapper.selectList(new LambdaQueryWrapper<InterviewDetail>()
                .eq(InterviewDetail::getInterviewId, interviewId));
        
        BigDecimal avgAccuracy = calculateAverage(details, d -> d.getScoreAccuracy());
        BigDecimal avgProfessional = calculateAverage(details, d -> d.getScoreProfessional());
        BigDecimal avgLogic = calculateAverage(details, d -> d.getScoreLogic());
        BigDecimal totalScore = calculateAverage(details, d -> d.getTotalScore());
        
        List<ScoreDetailDTO.QuestionScore> questionScores = details.stream()
                .map(detail -> {
                    Resource resource = resourceMapper.selectById(detail.getQuestionId());
                    return ScoreDetailDTO.QuestionScore.builder()
                            .questionId(detail.getQuestionId())
                            .question(resource != null ? resource.getTitle() : "未知题目")
                            .userText(detail.getUserText())
                            .answerMode(detail.getAnswerMode())
                            .accuracy(detail.getScoreAccuracy())
                            .professional(detail.getScoreProfessional())
                            .logic(detail.getScoreLogic())
                            .totalScore(detail.getTotalScore())
                            .build();
                })
                .collect(Collectors.toList());
        
        return ScoreDetailDTO.builder()
                .interviewId(interviewId)
                .jobName(job != null ? job.getJobName() : "未知岗位")
                .difficulty(interview.getDifficulty())
                .mode(interview.getMode())
                .plannedDuration(interview.getPlannedDuration())
                .actualDuration(interview.getActualDuration())
                .avgAccuracy(avgAccuracy)
                .avgProfessional(avgProfessional)
                .avgLogic(avgLogic)
                .totalScore(totalScore)
                .questionScores(questionScores)
                .build();
    }
    
    private BigDecimal calculateInterviewAverageScore(Long interviewId) {
        List<InterviewDetail> details = interviewDetailMapper.selectList(new LambdaQueryWrapper<InterviewDetail>()
                .eq(InterviewDetail::getInterviewId, interviewId));
        return calculateAverage(details, d -> d.getTotalScore());
    }
    
    private <T> BigDecimal calculateAverage(List<T> list, java.util.function.Function<T, BigDecimal> getter) {
        if (list == null || list.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal sum = list.stream()
                .map(getter)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int count = (int) list.stream().map(getter).filter(v -> v != null).count();
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }
}
