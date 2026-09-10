package com.nexjob.platform.service.impl;

import com.nexjob.platform.dto.request.SupportTicketRequest;
import com.nexjob.platform.dto.request.SupportTicketResolveRequest;
import com.nexjob.platform.dto.response.SupportTicketResponse;
import com.nexjob.platform.entity.SupportTicket;
import com.nexjob.platform.entity.User;
import com.nexjob.platform.enums.TicketStatus;
import com.nexjob.platform.exception.ResourceNotFoundException;
import com.nexjob.platform.mapper.SupportTicketMapper;
import com.nexjob.platform.repository.SupportTicketRepository;
import com.nexjob.platform.security.SecurityUtils;
import com.nexjob.platform.service.EmailService;
import com.nexjob.platform.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final EmailService emailService;
    private final SupportTicketMapper mapper;

    @Override
    @Transactional
    public SupportTicketResponse create(SupportTicketRequest request) {
        // El modulo de ayuda tambien es accesible sin sesion (endpoint publico), por eso el
        // usuario puede ser null: se identifica al reportante solo por su correo de contacto.
        User user = SecurityUtils.getCurrentUserOrNull();

        SupportTicket ticket = SupportTicket.builder()
                .user(user)
                .contactEmail(request.getContactEmail())
                .subject(request.getSubject())
                .category(request.getCategory())
                .message(request.getMessage())
                .build();
        ticket = supportTicketRepository.save(ticket);

        emailService.sendSupportTicketConfirmation(ticket.getContactEmail(), ticket.getSubject(), "TCK-" + ticket.getId());

        return mapper.toResponse(ticket);
    }

    @Override
    public Page<SupportTicketResponse> getMine(Pageable pageable) {
        User user = SecurityUtils.getCurrentUserOrNull();
        if (user == null) {
            throw new ResourceNotFoundException("No hay sesion activa");
        }
        return supportTicketRepository.findByUser_IdOrderByCreatedAtDesc(user.getId(), pageable).map(mapper::toResponse);
    }

    @Override
    public Page<SupportTicketResponse> adminList(TicketStatus status, Pageable pageable) {
        Page<SupportTicket> page = status == null
                ? supportTicketRepository.findAllByOrderByCreatedAtDesc(pageable)
                : supportTicketRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional
    public SupportTicketResponse adminResolve(Long id, SupportTicketResolveRequest request) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado: " + id));
        ticket.setStatus(request.getStatus());
        ticket.setAdminResponse(request.getAdminResponse());
        if (request.getStatus() == TicketStatus.RESUELTO || request.getStatus() == TicketStatus.CERRADO) {
            ticket.setResolvedAt(LocalDateTime.now());
            ticket.setResolvedBy(SecurityUtils.getCurrentUserOrNull());
        }
        return mapper.toResponse(supportTicketRepository.save(ticket));
    }
}
