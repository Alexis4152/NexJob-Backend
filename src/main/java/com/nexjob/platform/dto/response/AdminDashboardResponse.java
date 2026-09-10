package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AdminDashboardResponse {
    private long totalClients;
    private long totalProviders;
    private long totalBookings;
    private long bookingsInProgress;
    private long bookingsCompleted;
    private long openSupportTickets;
    private BigDecimal totalPaymentsReleased;
}
