package com.nexjob.platform.entity;

import com.nexjob.platform.enums.BookingStatus;
import com.nexjob.platform.enums.BookingUrgency;
import com.nexjob.platform.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Una contratacion (solicitud de servicio) entre un cliente y un prestador, atada a un
 * {@link ServiceOffering} concreto. El estado avanza a traves de {@link BookingStatus};
 * ver BookingServiceImpl.ALLOWED_TRANSITIONS para las transiciones validas.
 */
@Entity
@Table(name = "bookings")
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class Booking extends AuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String folio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_profile_id", nullable = false)
    private ProviderProfile provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_offering_id", nullable = false)
    private ServiceOffering service;

    @Column(name = "agreed_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal agreedPrice;

    @Column(length = 1000)
    private String description;

    @Column(name = "address_line", nullable = false, length = 255)
    private String addressLine;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    // Que tan pronto necesita el cliente el servicio; util para que el prestador priorice
    // sus solicitudes (ver BookingRequest / tablero del prestador).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingUrgency urgency;

    @Column(name = "cancelled_reason", length = 500)
    private String cancelledReason;
}
