package com.mockmaster.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("resource")
public class Resource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String resourceType;
    private String title;
    private String content;
    private String url;
    private Long jobId;
    private Integer difficulty;
}
