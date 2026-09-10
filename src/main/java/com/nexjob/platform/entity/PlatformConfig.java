package com.nexjob.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/** Fila unica con la configuracion de marca/contacto de la plataforma, editable por el ADMIN. */
@Entity
@Table(name = "platform_config")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PlatformConfig {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "platform_name", nullable = false, length = 150)
    private String platformName;

    @Column(name = "legal_name", length = 150)
    private String legalName;

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "primary_color", nullable = false, length = 10)
    private String primaryColor;

    @Column(name = "secondary_color", nullable = false, length = 10)
    private String secondaryColor;

    @Column(name = "welcome_message", length = 500)
    private String welcomeMessage;

    @Column(name = "footer_text", length = 500)
    private String footerText;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id")
    private User updatedBy;
}
