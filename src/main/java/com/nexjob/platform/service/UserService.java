package com.nexjob.platform.service;

import com.nexjob.platform.dto.request.UserUpdateRequest;
import com.nexjob.platform.dto.response.UserResponse;
import com.nexjob.platform.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse updateMyProfile(UserUpdateRequest request);
    Page<UserResponse> adminList(RoleName role, String q, Pageable pageable);
    UserResponse adminSetActive(Long id, boolean active);
}
