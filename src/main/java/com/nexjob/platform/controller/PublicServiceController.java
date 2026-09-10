package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.response.ServiceOfferingResponse;
import com.nexjob.platform.service.ServiceOfferingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/services")
@RequiredArgsConstructor
public class PublicServiceController {

    private final ServiceOfferingService serviceOfferingService;

    @GetMapping("/{id}")
    public ApiResponse<ServiceOfferingResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(serviceOfferingService.getPublicById(id));
    }
}
