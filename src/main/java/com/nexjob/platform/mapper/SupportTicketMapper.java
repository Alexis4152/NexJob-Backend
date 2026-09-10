package com.nexjob.platform.mapper;

import com.nexjob.platform.dto.response.SupportTicketResponse;
import com.nexjob.platform.entity.SupportTicket;
import org.springframework.stereotype.Component;

@Component
public class SupportTicketMapper {
    public SupportTicketResponse toResponse(SupportTicket t) {
        return SupportTicketResponse.builder()
                .id(t.getId())
                .contactEmail(t.getContactEmail())
                .subject(t.getSubject())
                .category(t.getCategory().name())
                .message(t.getMessage())
                .status(t.getStatus().name())
                .adminResponse(t.getAdminResponse())
                .createdAt(t.getCreatedAt())
                .resolvedAt(t.getResolvedAt())
                .build();
    }
}
