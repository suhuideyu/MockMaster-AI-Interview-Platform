package com.mockmaster.backend.controller;

import com.mockmaster.backend.common.ApiResponse;
import com.mockmaster.backend.entity.Job;
import com.mockmaster.backend.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ApiResponse<List<Job>> list() {
        return ApiResponse.success(jobService.listAll());
    }
}
