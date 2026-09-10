package com.nexjob.platform.mapper;

import com.nexjob.platform.dto.response.ReviewResponse;
import com.nexjob.platform.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    public ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .bookingId(r.getBooking().getId())
                .serviceId(r.getBooking().getService().getId())
                .serviceTitle(r.getBooking().getService().getTitle())
                .clientFirstName(r.getClient().getFirstName())
                .clientLastName(r.getClient().getLastName())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
