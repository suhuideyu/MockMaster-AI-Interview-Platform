package com.mockmaster.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mockmaster.backend.entity.Job;
import com.mockmaster.backend.mapper.JobMapper;
import com.mockmaster.backend.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobMapper jobMapper;

    @Override
    public List<Job> listAll() {
        return jobMapper.selectList(new LambdaQueryWrapper<Job>().orderByAsc(Job::getId));
    }

    @Override
    public Job getById(Long jobId) {
        return jobMapper.selectById(jobId);
    }
}
