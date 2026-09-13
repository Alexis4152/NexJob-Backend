package com.nexjob.platform.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ValidateResetCodeRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es valido")
    private String email;

    @NotBlank(message = "El codigo es obligatorio")
    private String code;
}
