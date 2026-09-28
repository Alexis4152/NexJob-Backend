package com.nexjob.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** Codigo de 6 digitos para que un usuario ya autenticado confirme que su correo es suyo (ver
 * UserServiceImpl). Mismo patron que PasswordResetToken: el codigo se guarda como hash, nunca
 * en texto plano. No es un requisito para usar la plataforma, solo una senal de confianza
 * opcional que el usuario activa desde su perfil. */
@Entity
@Table(name = "email_verification_codes")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class EmailVerificationCode {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Hash SHA-256 del codigo (nunca se guarda en texto plano).
    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
