package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.request.ProviderProfileRequest;
import com.nexjob.platform.dto.response.ProviderSelfResponse;
import com.nexjob.platform.service.ProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/provider/profile")
@RequiredArgsConstructor
public class ProviderProfileController {

    private final ProviderService providerService;

    @GetMapping
    public ApiResponse<ProviderSelfResponse> getMine() {
        return ApiResponse.ok(providerService.getMyProfile());
    }

    @PutMapping
    public ApiResponse<ProviderSelfResponse> update(@Valid @RequestBody ProviderProfileRequest request) {
        return ApiResponse.ok(providerService.updateMyProfile(request), "Perfil actualizado");
    }

    @PostMapping(value = "/image", consumes = "multipart/form-data")
    public ApiResponse<ProviderSelfResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(providerService.updateMyProfileImage(file), "Foto de perfil actualizada");
    }
}
