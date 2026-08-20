package com.mockmaster.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("interview_detail")
public class InterviewDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long interviewId;
    private Long questionId;
    private String userText;

    private BigDecimal scoreAccuracy;
    private BigDecimal scoreProfessional;
    private BigDecimal scoreLogic;
    private BigDecimal totalScore;
    
    private String audioUrl;
    private String answerMode;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
