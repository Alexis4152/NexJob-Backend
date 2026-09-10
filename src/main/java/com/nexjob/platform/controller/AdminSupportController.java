package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.PageResponse;
import com.nexjob.platform.dto.request.SupportTicketResolveRequest;
import com.nexjob.platform.dto.response.SupportTicketResponse;
import com.nexjob.platform.enums.TicketStatus;
import com.nexjob.platform.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/support/tickets")
@RequiredArgsConstructor
public class AdminSupportController {

    private final SupportTicketService supportTicketService;

    @GetMapping
    public ApiResponse<PageResponse<SupportTicketResponse>> list(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(PageResponse.of(supportTicketService.adminList(status, PageRequest.of(page, size))));
    }

    @PatchMapping("/{id}/resolve")
    public ApiResponse<SupportTicketResponse> resolve(@PathVariable Long id, @Valid @RequestBody SupportTicketResolveRequest request) {
        return ApiResponse.ok(supportTicketService.adminResolve(id, request), "Ticket actualizado");
    }
}
