package com.nexjob.platform.service;

import com.nexjob.platform.dto.request.SupportTicketRequest;
import com.nexjob.platform.dto.request.SupportTicketResolveRequest;
import com.nexjob.platform.dto.response.SupportTicketResponse;
import com.nexjob.platform.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupportTicketService {
    SupportTicketResponse create(SupportTicketRequest request);
    Page<SupportTicketResponse> getMine(Pageable pageable);
    Page<SupportTicketResponse> adminList(TicketStatus status, Pageable pageable);
    SupportTicketResponse adminResolve(Long id, SupportTicketResolveRequest request);
}
