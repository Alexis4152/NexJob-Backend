package com.nexjob.platform.service;

import com.nexjob.platform.dto.request.ForgotPasswordRequest;
import com.nexjob.platform.dto.request.LoginRequest;
import com.nexjob.platform.dto.request.RegisterProviderRequest;
import com.nexjob.platform.dto.request.RegisterRequest;
import com.nexjob.platform.dto.request.ResetPasswordRequest;
import com.nexjob.platform.dto.request.ValidateResetCodeRequest;
import com.nexjob.platform.dto.response.LoginResponse;
import com.nexjob.platform.entity.User;

public interface AuthService {
    LoginResponse register(RegisterRequest request);
    LoginResponse registerProvider(RegisterProviderRequest request);
    LoginResponse login(LoginRequest request);
    User getCurrentUser();
    void forgotPassword(ForgotPasswordRequest request, String clientIp);
    void validateResetCode(ValidateResetCodeRequest request, String clientIp);
    void resetPassword(ResetPasswordRequest request, String clientIp);
}
