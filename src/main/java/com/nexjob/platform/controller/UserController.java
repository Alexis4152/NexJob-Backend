package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.request.UserUpdateRequest;
import com.nexjob.platform.dto.response.UserResponse;
import com.nexjob.platform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(@Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.ok(userService.updateMyProfile(request), "Perfil actualizado");
    }

    @PostMapping(value = "/me/photo", consumes = "multipart/form-data")
    public ApiResponse<UserResponse> uploadPhoto(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(userService.updateMyPhoto(file), "Foto de perfil actualizada");
    }
}
