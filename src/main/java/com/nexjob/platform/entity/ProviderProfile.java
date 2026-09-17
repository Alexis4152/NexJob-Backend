package com.nexjob.platform.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Perfil extendido de un {@link User} con rol PROVIDER: describe su oficio, cobertura y
 * reputacion. {@code averageRating}/{@code totalReviews} se recalculan cada vez que se
 * registra una {@link Review} nueva (ver ReviewServiceImpl) para no tener que agregar sobre
 * toda la tabla de resenas en cada busqueda de catalogo.
 */
@Entity
@Table(name = "provider_profiles")
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class ProviderProfile extends AuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @Column(length = 1000)
    private String bio;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(length = 100)
    private String city;

    // Codigo postal capturado por el prestador; se usa para calcular latitude/longitude
    // via PostalCodeLookupService (mas preciso que el centroide por ciudad). Opcional.
    @Column(name = "postal_code", length = 10)
    private String postalCode;

    // Coordenadas aproximadas usadas para el filtro/orden de distancia: vienen del codigo
    // postal cuando esta capturado, o del centroide de la ciudad como respaldo (ver
    // db/05_add_geo_columns.sql). Nulas hasta que se pueda calcular alguna de las dos.
    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews", nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    // Los siguientes 3 solo los autoriza un administrador desde el panel admin (no son
    // auto-computados ni editables por el propio prestador); alimentan la seccion de
    // "Confianza" del perfil publico (ver ProviderDetail en el frontend).
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    @Builder.Default
    private Boolean phoneVerified = false;

    @Column(name = "profile_complete", nullable = false)
    @Builder.Default
    private Boolean profileComplete = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "provider_categories",
            joinColumns = @JoinColumn(name = "provider_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();
}
