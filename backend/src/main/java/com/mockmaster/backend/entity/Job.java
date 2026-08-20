package com.mockmaster.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("job")
public class Job {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("job_name")
    private String jobName;

    @TableField("job_desc")
    private String jobDesc;

    private LocalDateTime createTime;
}
