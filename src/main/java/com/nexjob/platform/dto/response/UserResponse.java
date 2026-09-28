package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String city;
    private String postalCode;
    private Integer age;
    private String profileImageUrl;
    private Boolean emailVerified;
    private String role;
    private Boolean isActive;
    private Long providerProfileId;
    private LocalDateTime createdAt;
}
