package com.nexjob.platform.service.impl;

import com.nexjob.platform.dto.request.UserUpdateRequest;
import com.nexjob.platform.dto.response.UserResponse;
import com.nexjob.platform.entity.User;
import com.nexjob.platform.enums.RoleName;
import com.nexjob.platform.exception.ResourceNotFoundException;
import com.nexjob.platform.mapper.UserMapper;
import com.nexjob.platform.repository.UserRepository;
import com.nexjob.platform.security.SecurityUtils;
import com.nexjob.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse updateMyProfile(UserUpdateRequest request) {
        User user = currentUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public Page<UserResponse> adminList(RoleName role, String q, Pageable pageable) {
        Page<User> page = (q == null || q.isBlank())
                ? userRepository.findByRole_Name(role, pageable)
                : userRepository.findByRole_NameAndEmailContainingIgnoreCase(role, q.trim(), pageable);
        return page.map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse adminSetActive(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        user.setIsActive(active);
        user.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        return userMapper.toResponse(userRepository.save(user));
    }

    private User currentUser() {
        User user = SecurityUtils.getCurrentUserOrNull();
        if (user == null) {
            throw new ResourceNotFoundException("No hay sesion activa");
        }
        return user;
    }
}
