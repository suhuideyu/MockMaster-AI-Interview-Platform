package com.mockmaster.backend.controller;

import com.mockmaster.backend.common.ApiResponse;
import com.mockmaster.backend.entity.Resource;
import com.mockmaster.backend.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping("/questions")
    public ApiResponse<List<Resource>> getQuestions(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Integer difficulty) {
        return ApiResponse.success(resourceService.getQuestions(jobId, difficulty));
    }

    @GetMapping("/{id}")
    public ApiResponse<Resource> getById(@PathVariable Long id) {
        return ApiResponse.success(resourceService.getById(id));
    }

    @GetMapping
    public ApiResponse<List<Resource>> listAll() {
        return ApiResponse.success(resourceService.listAll());
    }
}
