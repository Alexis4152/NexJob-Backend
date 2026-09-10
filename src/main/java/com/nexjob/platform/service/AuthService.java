package com.nexjob.platform.service;

import com.nexjob.platform.dto.request.LoginRequest;
import com.nexjob.platform.dto.request.RegisterProviderRequest;
import com.nexjob.platform.dto.request.RegisterRequest;
import com.nexjob.platform.dto.response.LoginResponse;
import com.nexjob.platform.entity.User;

public interface AuthService {
    LoginResponse register(RegisterRequest request);
    LoginResponse registerProvider(RegisterProviderRequest request);
    LoginResponse login(LoginRequest request);
    User getCurrentUser();
}
