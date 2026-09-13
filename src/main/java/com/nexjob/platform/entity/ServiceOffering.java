package com.nexjob.platform.entity;

import com.nexjob.platform.enums.DurationUnit;
import com.nexjob.platform.enums.PriceType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/** Un servicio especifico que un prestador ofrece dentro de una categoria (ej. "Instalacion de closets"). */
@Entity
@Table(name = "service_offerings")
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class ServiceOffering extends AuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_profile_id", nullable = false)
    private ProviderProfile provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false, length = 20)
    private PriceType priceType;

    @Column(name = "estimated_duration_value")
    private Integer estimatedDurationValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "estimated_duration_unit", nullable = false, length = 20)
    @Builder.Default
    private DurationUnit estimatedDurationUnit = DurationUnit.MINUTOS;

    // true: el prestador se traslada al domicilio del cliente. false: el cliente debe acudir
    // con el prestador.
    @Column(name = "at_client_location", nullable = false)
    @Builder.Default
    private Boolean atClientLocation = true;
}
