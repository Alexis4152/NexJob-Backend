package com.nexjob.platform.mapper;

import com.nexjob.platform.dto.response.UserResponse;
import com.nexjob.platform.entity.User;
import com.nexjob.platform.repository.ProviderProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final ProviderProfileRepository providerProfileRepository;

    public UserResponse toResponse(User u) {
        Long providerProfileId = providerProfileRepository.findByUser_Id(u.getId())
                .map(p -> p.getId())
                .orElse(null);
        return UserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .phone(u.getPhone())
                .role(u.getRole().getName().name())
                .isActive(u.getIsActive())
                .providerProfileId(providerProfileId)
                .build();
    }
}
