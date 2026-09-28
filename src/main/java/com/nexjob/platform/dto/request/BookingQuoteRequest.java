package com.nexjob.platform.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingQuoteRequest {

    @NotEmpty(message = "Agrega al menos un concepto a la cotizacion")
    @Valid
    private List<BookingQuoteItemRequest> items;

    private String note;

    // Opcional: cuando el prestador estima que terminaria/entregaria el trabajo. Es solo una
    // estimacion para que el cliente decida si le sirve, no la fecha real de la visita (esa se
    // acuerda hasta que se acepta la cotizacion).
    private LocalDate estimatedDeliveryDate;
}
