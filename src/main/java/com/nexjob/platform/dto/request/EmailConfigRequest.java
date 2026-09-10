package com.nexjob.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailConfigRequest {

    @NotNull(message = "El campo habilitado es obligatorio")
    private Boolean enabled;

    @NotBlank(message = "El host SMTP es obligatorio")
    private String smtpHost;

    @NotNull(message = "El puerto SMTP es obligatorio")
    private Integer smtpPort;

    private String smtpUsername;
    private String smtpPassword;
    private String fromAddress;
}
