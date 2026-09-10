package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.PageResponse;
import com.nexjob.platform.dto.request.StatusToggleRequest;
import com.nexjob.platform.dto.response.ProviderSelfResponse;
import com.nexjob.platform.service.ProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/providers")
@RequiredArgsConstructor
public class AdminProviderController {

    private final ProviderService providerService;

    @GetMapping
    public ApiResponse<PageResponse<ProviderSelfResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(PageResponse.of(providerService.adminList(q, PageRequest.of(page, size))));
    }

    @PatchMapping("/{id}/verified")
    public ApiResponse<ProviderSelfResponse> setVerified(@PathVariable Long id, @Valid @RequestBody StatusToggleRequest request) {
        return ApiResponse.ok(providerService.adminSetVerified(id, request.getValue()), "Estado de verificacion actualizado");
    }
}
