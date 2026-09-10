package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long bookingId;
    private Long serviceId;
    private String serviceTitle;
    private String clientFirstName;
    private String clientLastName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
