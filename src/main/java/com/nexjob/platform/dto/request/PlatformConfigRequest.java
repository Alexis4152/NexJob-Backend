package com.nexjob.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlatformConfigRequest {

    @NotBlank(message = "El nombre de la plataforma es obligatorio")
    private String platformName;

    private String legalName;
    private String contactEmail;
    private String contactPhone;

    @NotBlank(message = "El color primario es obligatorio")
    private String primaryColor;

    @NotBlank(message = "El color secundario es obligatorio")
    private String secondaryColor;

    private String welcomeMessage;
    private String footerText;
}
