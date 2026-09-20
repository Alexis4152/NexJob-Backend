package com.nexjob.platform.dto.request;

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
    @Size(max = 60, message = "El correo no puede tener mas de 60 caracteres")
    @Pattern(regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$",
            message = "El formato del campo correo electronico es erroneo. Ejemplo: correo_electronico@mail.com.mx")
    private String email;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    // Opcional: si se captura, debe ser un numero de 10 digitos ("^$" acepta vacio).
    @Pattern(regexp = "^$|\\d{10}", message = "El telefono debe tener 10 digitos numericos")
    private String phone;

    @NotBlank(message = "El nombre del negocio u oficio es obligatorio")
    private String businessName;

    private String bio;

    private Integer yearsExperience;

    // Opcional al registrarse: se puede completar despues desde "Mi perfil".
    private String city;

    // Opcional: si se captura, se usa para ubicar al prestador con mas precision que la
    // ciudad (ver PostalCodeLookupService). "^$|\\d{5}" acepta vacio o 5 digitos.
    @Pattern(regexp = "^$|\\d{5}", message = "El codigo postal debe tener 5 digitos")
    private String postalCode;

    @NotEmpty(message = "Selecciona al menos una categoria de servicio")
    private List<Long> categoryIds;
}
