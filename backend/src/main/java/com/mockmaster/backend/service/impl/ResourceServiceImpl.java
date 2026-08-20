package com.mockmaster.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mockmaster.backend.entity.Resource;
import com.mockmaster.backend.mapper.ResourceMapper;
import com.mockmaster.backend.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceMapper resourceMapper;

    @Override
    public List<Resource> getQuestions(Long jobId, Integer difficulty) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resource::getResourceType, "question");

        if (jobId != null && jobId > 0) {
            wrapper.eq(Resource::getJobId, jobId);
        }
        if (difficulty != null) {
            wrapper.eq(Resource::getDifficulty, difficulty);
        }

        wrapper.orderByAsc(Resource::getId);
        return resourceMapper.selectList(wrapper);
    }

    @Override
    public List<Resource> listAll() {
        return resourceMapper.selectList(new LambdaQueryWrapper<Resource>().orderByAsc(Resource::getId));
    }

    @Override
    public Resource getById(Long id) {
        return resourceMapper.selectById(id);
    }
}
