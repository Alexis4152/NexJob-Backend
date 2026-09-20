package com.nexjob.platform.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Cotizacion que un prestador envia en respuesta a una QuoteRequest. */
@Data
public class QuoteSubmitRequest {

    @NotNull(message = "Elige a cual de tus servicios corresponde esta cotizacion")
    private Long serviceOfferingId;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal price;

    @NotNull(message = "Indica cuando podrias atenderlo")
    @Future(message = "La disponibilidad debe ser posterior a la actual")
    private LocalDateTime availableAt;

    private String note;
}
