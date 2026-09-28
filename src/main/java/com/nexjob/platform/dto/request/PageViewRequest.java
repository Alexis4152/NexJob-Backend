package com.nexjob.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PageViewRequest {

    @NotBlank(message = "El visitorId es obligatorio")
    private String visitorId;

    @NotBlank(message = "La ruta es obligatoria")
    private String path;

    private String referrer;
}
