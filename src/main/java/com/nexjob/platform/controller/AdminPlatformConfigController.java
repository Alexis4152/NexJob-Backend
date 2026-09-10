package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.request.PlatformConfigRequest;
import com.nexjob.platform.dto.response.PlatformConfigResponse;
import com.nexjob.platform.service.PlatformConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/platform-config")
@RequiredArgsConstructor
public class AdminPlatformConfigController {

    private final PlatformConfigService platformConfigService;

    @GetMapping
    public ApiResponse<PlatformConfigResponse> get() {
        return ApiResponse.ok(platformConfigService.get());
    }

    @PutMapping
    public ApiResponse<PlatformConfigResponse> update(@Valid @RequestBody PlatformConfigRequest request) {
        return ApiResponse.ok(platformConfigService.update(request), "Configuracion actualizada");
    }

    @PostMapping(value = "/logo", consumes = "multipart/form-data")
    public ApiResponse<PlatformConfigResponse> uploadLogo(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(platformConfigService.updateLogo(file), "Logo actualizado");
    }
}
