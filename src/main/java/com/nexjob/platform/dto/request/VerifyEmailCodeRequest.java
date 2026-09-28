package com.nexjob.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyEmailCodeRequest {

    @NotBlank(message = "El codigo es obligatorio")
    private String code;
}
