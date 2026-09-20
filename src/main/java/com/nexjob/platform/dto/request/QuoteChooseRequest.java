package com.nexjob.platform.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** El cliente elige una de las cotizaciones recibidas; se convierte en una contratacion real. */
@Data
public class QuoteChooseRequest {

    @NotNull(message = "Indica que cotizacion eliges")
    private Long quoteId;
}
