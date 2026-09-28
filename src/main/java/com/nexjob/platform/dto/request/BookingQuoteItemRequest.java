package com.nexjob.platform.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookingQuoteItemRequest {

    @NotBlank(message = "El concepto es obligatorio")
    private String concept;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    private BigDecimal quantity;

    @NotBlank(message = "La unidad es obligatoria")
    private String unit;

    @NotNull(message = "El costo unitario es obligatorio")
    @DecimalMin(value = "0.0", message = "El costo no puede ser negativo")
    private BigDecimal unitCost;
}
