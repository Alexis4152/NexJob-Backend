package com.nexjob.platform.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Registro publico de un prestador de servicio: crea el usuario (rol PROVIDER) y su
 * {@code ProviderProfile} en una sola transaccion (ver AuthServiceImpl.registerProvider).
 */
@Data
public class RegisterProviderRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es valido")
    private String email;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    private String phone;

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
