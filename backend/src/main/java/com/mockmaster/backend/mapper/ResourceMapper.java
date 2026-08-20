package com.mockmaster.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mockmaster.backend.entity.Resource;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ResourceMapper extends BaseMapper<Resource> {
    
    /**
     * 获取指定岗位和难度下，该次面试中未回答过的随机题目
     * 
     * @param jobId
     * @param difficulty
     * @param interviewId
     * @return
     */
    @Select({
        "<script>",
        "SELECT * FROM resource r",
        "WHERE r.resource_type = 'question'",
        "AND r.job_id = #{jobId}",
        "AND r.difficulty = #{difficulty}",
        "AND r.id NOT IN (",
        "  SELECT question_id FROM interview_detail",
        "  WHERE interview_id = #{interviewId}",
        ")",
        "ORDER BY RAND()",
        "LIMIT 1",
        "</script>"
    })
    Resource getRandomUnusedQuestion(
            @Param("jobId") Long jobId,
            @Param("difficulty") Integer difficulty,
            @Param("interviewId") Long interviewId
    );
}

