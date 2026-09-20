package com.nexjob.platform.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    private String phone;

    private String city;

    // Opcional: "^$|\\d{5}" acepta vacio o 5 digitos.
    @Pattern(regexp = "^$|\\d{5}", message = "El codigo postal debe tener 5 digitos")
    private String postalCode;

    @Min(value = 1, message = "La edad no es valida")
    @Max(value = 120, message = "La edad no es valida")
    private Integer age;
}
