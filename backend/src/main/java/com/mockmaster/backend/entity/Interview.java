package com.mockmaster.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("interview")
public class Interview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long jobId;
    private String difficulty;
    private Integer plannedDuration;
    private Integer actualDuration;
    private String mode;
    private String status;
    private String summary;
    private String lastQuestion;
    private BigDecimal totalScore;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @TableField("round_count")
    private Integer roundCount;
}
