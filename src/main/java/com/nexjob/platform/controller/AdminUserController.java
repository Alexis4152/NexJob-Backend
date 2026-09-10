package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.PageResponse;
import com.nexjob.platform.dto.request.StatusToggleRequest;
import com.nexjob.platform.dto.response.UserResponse;
import com.nexjob.platform.enums.RoleName;
import com.nexjob.platform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> list(
            @RequestParam(defaultValue = "CLIENT") RoleName role,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = userService.adminList(role, q, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ApiResponse.ok(PageResponse.of(result));
    }

    @PatchMapping("/{id}/active")
    public ApiResponse<UserResponse> setActive(@PathVariable Long id, @Valid @RequestBody StatusToggleRequest request) {
        return ApiResponse.ok(userService.adminSetActive(id, request.getValue()), "Estado del usuario actualizado");
    }
}
