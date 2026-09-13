package com.nexjob.platform.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es valido")
    private String email;

    @NotBlank(message = "El codigo es obligatorio")
    private String code;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
    private String newPassword;
}
