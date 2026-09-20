package com.nexjob.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Registro publico de un cliente que busca contratar servicios. El rol siempre es CLIENT. */
@Data
public class RegisterRequest {

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
}
