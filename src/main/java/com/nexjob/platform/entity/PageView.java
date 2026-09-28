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
 * Una visita anonima a una pagina publica de la plataforma: no esta ligada a ningun usuario
 * registrado, solo a un {@code visitorId} generado y guardado en el navegador del visitante
 * (ver AnalyticsTracker.jsx). Sirve para que el admin sepa cuanto trafico real llega al sitio,
 * sin depender de que la gente se registre.
 */
@Entity
@Table(name = "page_views")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PageView {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visitor_id", nullable = false, length = 64)
    private String visitorId;

    @Column(nullable = false, length = 255)
    private String path;

    @Column(length = 500)
    private String referrer;

    // Segundos que el visitante permanecio en la pagina; nulo hasta que el navegador reporta la
    // duracion al salir/navegar (ver POST /pageview/{id}/duration). Es una aproximacion, no un
    // cronometro exacto: el navegador puede cerrarse sin llegar a reportarla.
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
