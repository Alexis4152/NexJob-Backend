package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.request.EmailConfigRequest;
import com.nexjob.platform.dto.response.EmailConfigResponse;
import com.nexjob.platform.service.EmailConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/email-config")
@RequiredArgsConstructor
public class AdminEmailConfigController {

    private final EmailConfigService emailConfigService;

    @GetMapping
    public ApiResponse<EmailConfigResponse> get() {
        return ApiResponse.ok(emailConfigService.get());
    }

    @PutMapping
    public ApiResponse<EmailConfigResponse> update(@Valid @RequestBody EmailConfigRequest request) {
        return ApiResponse.ok(emailConfigService.update(request), "Configuracion de correo actualizada");
    }
}
