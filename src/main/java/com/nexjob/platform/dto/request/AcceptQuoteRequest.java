package com.nexjob.platform.dto.request;

import com.nexjob.platform.enums.DeliveryMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Datos que el cliente confirma al aceptar una cotizacion: como se le hace llegar el trabajo.
 * Direccion/ciudad son obligatorias solo si elige DOMICILIO; si elige RECOGER_SITIO se usa la
 * ciudad registrada del prestador (ver BookingServiceImpl.acceptQuote).
 */
@Data
public class AcceptQuoteRequest {

    @NotNull(message = "Indica como se te hace llegar el trabajo")
    private DeliveryMethod deliveryMethod;

    private String addressLine;

    private String city;
}
