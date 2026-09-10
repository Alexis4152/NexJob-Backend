package com.nexjob.platform.dto.request;

import com.nexjob.platform.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SupportTicketResolveRequest {

    @NotNull(message = "El estado es obligatorio")
    private TicketStatus status;

    private String adminResponse;
}
