package com.nexjob.platform.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Usado por endpoints de administracion que solo activan/desactivan un recurso (ej. verificar prestador). */
@Data
public class StatusToggleRequest {

    @NotNull(message = "El valor es obligatorio")
    private Boolean value;
}
