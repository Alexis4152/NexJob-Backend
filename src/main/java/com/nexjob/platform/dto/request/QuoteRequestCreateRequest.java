package com.nexjob.platform.dto.request;

import com.nexjob.platform.enums.BookingUrgency;
import com.nexjob.platform.enums.PaymentMethod;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

/** Solicitud del cliente para recibir cotizaciones de varios prestadores de una categoria. */
@Data
public class QuoteRequestCreateRequest {

    @NotNull(message = "La categoria es obligatoria")
    private Long categoryId;

    @NotBlank(message = "Describe brevemente lo que necesitas")
    private String description;

    @NotBlank(message = "La direccion de la visita es obligatoria")
    private String addressLine;

    @NotBlank(message = "La ciudad es obligatoria")
    private String city;

    // Opcional: si se captura, se usa para elegir prestadores por distancia real en vez de
    // solo por categoria. "^$|\\d{5}" acepta vacio o 5 digitos.
    @Pattern(regexp = "^$|\\d{5}", message = "El codigo postal debe tener 5 digitos")
    private String postalCode;

    @NotNull(message = "La fecha en la que necesitas el servicio es obligatoria")
    @Future(message = "La fecha debe ser posterior a la actual")
    private LocalDateTime scheduledAt;

    @NotNull(message = "El metodo de pago es obligatorio")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Indica que tan pronto necesitas el servicio")
    private BookingUrgency urgency;
}
