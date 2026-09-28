package com.nexjob.platform.entity;

import com.nexjob.platform.enums.BookingStatus;
import com.nexjob.platform.enums.BookingUrgency;
import com.nexjob.platform.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // Nulos para servicios "a cotizar" hasta que se acepte la cotizacion (ver
    // BookingServiceImpl.create): no tiene sentido pedir direccion/fecha de visita antes de
    // que exista un precio acordado.
    @Column(name = "address_line", length = 255)
    private String addressLine;

    @Column(length = 100)
    private String city;

    // Foto opcional que el cliente adjunta al solicitar (ej. el mueble que le gusta, la fuga
    // que tiene), para complementar la descripcion. Distinta de BookingEvidence, que sube el
    // prestador para demostrar el trabajo ya hecho.
    @Column(name = "reference_image_url", length = 500)
    private String referenceImageUrl;

    // Desglose de materiales/mano de obra que el prestador envia cuando el servicio es "a
    // cotizar" (ver ServiceOffering.priceType), ademas de la nota opcional y el total ya
    // calculado. Vacios/null hasta que el prestador cotiza (estado COTIZADO).
    @Convert(converter = BookingQuoteItemsConverter.class)
    @Column(name = "quote_items_json", columnDefinition = "TEXT")
    @Builder.Default
    private List<BookingQuoteItem> quoteItems = new ArrayList<>();

    @Column(name = "quote_note", length = 1000)
    private String quoteNote;

    @Column(name = "quote_total", precision = 12, scale = 2)
    private BigDecimal quoteTotal;

    @Column(name = "quote_sent_at")
    private LocalDateTime quoteSentAt;

    // Estimacion del prestador de cuando terminaria/entregaria el trabajo (ver
    // BookingServiceImpl.submitQuote); no es la fecha real de la visita, esa se agenda hasta
    // que se acepta la cotizacion.
    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    @Column(name = "scheduled_at")
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
