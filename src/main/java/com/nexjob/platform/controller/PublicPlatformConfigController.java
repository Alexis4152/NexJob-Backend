package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.response.PlatformConfigResponse;
import com.nexjob.platform.service.PlatformConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/platform-config")
@RequiredArgsConstructor
public class PublicPlatformConfigController {

    private final PlatformConfigService platformConfigService;

    @GetMapping
    public ApiResponse<PlatformConfigResponse> get() {
        return ApiResponse.ok(platformConfigService.get());
    }
}
