package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.request.ForgotPasswordRequest;
import com.nexjob.platform.dto.request.LoginRequest;
import com.nexjob.platform.dto.request.RegisterProviderRequest;
import com.nexjob.platform.dto.request.RegisterRequest;
import com.nexjob.platform.dto.request.ResetPasswordRequest;
import com.nexjob.platform.dto.request.ValidateResetCodeRequest;
import com.nexjob.platform.dto.response.LoginResponse;
import com.nexjob.platform.mapper.UserMapper;
import com.nexjob.platform.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Cuenta creada correctamente"));
    }

    @PostMapping("/register-provider")
    public ResponseEntity<ApiResponse<LoginResponse>> registerProvider(@Valid @RequestBody RegisterProviderRequest request) {
        LoginResponse response = authService.registerProvider(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Cuenta de prestador creada correctamente"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> me() {
        return ResponseEntity.ok(ApiResponse.ok(userMapper.toResponse(authService.getCurrentUser())));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        authService.forgotPassword(request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok(null, "Si el correo esta registrado, te enviamos un codigo para restablecer tu contrasena"));
    }

    @PostMapping("/validate-reset-code")
    public ResponseEntity<ApiResponse<Void>> validateResetCode(@Valid @RequestBody ValidateResetCodeRequest request, HttpServletRequest httpRequest) {
        authService.validateResetCode(request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok(null, "Codigo valido"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
        authService.resetPassword(request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok(null, "Tu contrasena fue actualizada correctamente"));
    }
}
