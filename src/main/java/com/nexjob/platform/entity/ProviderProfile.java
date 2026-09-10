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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "provider_categories",
            joinColumns = @JoinColumn(name = "provider_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();
}
