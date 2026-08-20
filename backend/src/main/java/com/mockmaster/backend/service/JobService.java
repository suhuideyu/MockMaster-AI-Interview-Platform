package com.mockmaster.backend.service;

import com.mockmaster.backend.entity.Job;

import java.util.List;

public interface JobService {
    List<Job> listAll();

    Job getById(Long jobId);
}
