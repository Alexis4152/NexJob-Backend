package com.nexjob.platform.repository;

import com.nexjob.platform.entity.SupportTicket;
import com.nexjob.platform.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    Page<SupportTicket> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status, Pageable pageable);
    Page<SupportTicket> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long countByStatusIn(List<TicketStatus> statuses);
}
