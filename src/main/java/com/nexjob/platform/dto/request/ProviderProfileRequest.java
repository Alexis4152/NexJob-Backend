package com.nexjob.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
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

    // Opcional: si se captura, se usa para ubicar al prestador con mas precision que la
    // ciudad (ver PostalCodeLookupService). "^$|\\d{5}" acepta vacio o 5 digitos.
    @Pattern(regexp = "^$|\\d{5}", message = "El codigo postal debe tener 5 digitos")
    private String postalCode;

    @NotEmpty(message = "Selecciona al menos una categoria de servicio")
    private List<Long> categoryIds;
}
