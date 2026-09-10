package com.nexjob.platform.service.impl;

import com.nexjob.platform.dto.response.AdminDashboardResponse;
import com.nexjob.platform.enums.BookingStatus;
import com.nexjob.platform.enums.PaymentStatus;
import com.nexjob.platform.enums.RoleName;
import com.nexjob.platform.enums.TicketStatus;
import com.nexjob.platform.repository.BookingRepository;
import com.nexjob.platform.repository.PaymentRepository;
import com.nexjob.platform.repository.SupportTicketRepository;
import com.nexjob.platform.repository.UserRepository;
import com.nexjob.platform.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public AdminDashboardResponse getAdminDashboard() {
        return AdminDashboardResponse.builder()
                .totalClients(userRepository.countByRole_Name(RoleName.CLIENT))
                .totalProviders(userRepository.countByRole_Name(RoleName.PROVIDER))
                .totalBookings(bookingRepository.count())
                .bookingsInProgress(bookingRepository.countByStatus(BookingStatus.EN_PROCESO))
                .bookingsCompleted(bookingRepository.countByStatus(BookingStatus.APROBADO))
                .openSupportTickets(supportTicketRepository.countByStatusIn(List.of(TicketStatus.ABIERTO, TicketStatus.EN_REVISION)))
                .totalPaymentsReleased(paymentRepository.sumAmountByStatus(PaymentStatus.LIBERADO))
                .build();
    }
}
