package com.mockmaster.backend.service;

import com.mockmaster.backend.entity.Resource;

import java.util.List;

public interface ResourceService {
    List<Resource> getQuestions(Long jobId, Integer difficulty);

    List<Resource> listAll();

    Resource getById(Long id);
}
