package com.nexjob.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ProviderProfileRequest {

    @NotBlank(message = "El nombre del negocio u oficio es obligatorio")
    private String businessName;

    private String bio;

    private Integer yearsExperience;

    @NotBlank(message = "La ciudad de cobertura es obligatoria")
    private String city;

    @NotEmpty(message = "Selecciona al menos una categoria de servicio")
    private List<Long> categoryIds;
}
