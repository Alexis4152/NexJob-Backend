package com.nexjob.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Registro de a que prestador se le envio una {@link QuoteRequest} (el sistema elige hasta 5
 * automaticamente por categoria, igual criterio que "Recomendados" en la busqueda). Permite que
 * el cliente vea el avance ("3 de 5 ya cotizaron") con una lista fija, y que el prestador pueda
 * declinar sin tener que enviar una cotizacion.
 */
@Entity
@Table(name = "quote_request_recipients")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class QuoteRequestRecipient {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_request_id", nullable = false)
    private QuoteRequest quoteRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_profile_id", nullable = false)
    private ProviderProfile provider;

    @Column(nullable = false)
    @Builder.Default
    private Boolean declined = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
