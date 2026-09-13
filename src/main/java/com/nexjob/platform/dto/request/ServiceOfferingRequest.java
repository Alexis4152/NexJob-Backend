package com.nexjob.platform.dto.request;

import com.nexjob.platform.enums.DurationUnit;
import com.nexjob.platform.enums.PriceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceOfferingRequest {

    @NotNull(message = "La categoria es obligatoria")
    private Long categoryId;

    @NotBlank(message = "El titulo es obligatorio")
    private String title;

    @Size(max = 500, message = "La descripcion no puede superar 500 caracteres")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private BigDecimal price;

    @NotNull(message = "El tipo de precio es obligatorio")
    private PriceType priceType;

    private Integer estimatedDurationValue;

    private DurationUnit estimatedDurationUnit;

    private Boolean atClientLocation;
}
