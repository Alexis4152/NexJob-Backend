package com.nexjob.platform.entity;

import com.nexjob.platform.enums.BookingUrgency;
import com.nexjob.platform.enums.PaymentMethod;
import com.nexjob.platform.enums.QuoteRequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Solicitud del cliente para recibir cotizaciones de varios prestadores de una categoria
 * (punto 18), en vez de elegir uno solo desde el buscador. Al elegir una {@link Quote} se crea
 * una {@link Booking} real con ese prestador (ver QuoteRequestServiceImpl.choose).
 */
@Entity
@Table(name = "quote_requests")
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class QuoteRequest extends AuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(length = 1000)
    private String description;

    @Column(name = "address_line", nullable = false, length = 255)
    private String addressLine;

    @Column(nullable = false, length = 100)
    private String city;

    // Opcional: si se captura, se usa junto con PostalCodeLookupService para calcular
    // latitude/longitude y elegir prestadores por distancia real en vez de solo por categoria.
    @Column(name = "postal_code", length = 5)
    private String postalCode;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingUrgency urgency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuoteRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resulting_booking_id")
    private Booking resultingBooking;
}
