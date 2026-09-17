package com.nexjob.platform.dto.request;

import com.nexjob.platform.enums.BookingUrgency;
import com.nexjob.platform.enums.PaymentMethod;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/** Solicitud de contratacion de un servicio: agenda la visita y describe lo que el cliente necesita. */
@Data
public class BookingRequest {

    @NotNull(message = "El servicio es obligatorio")
    private Long serviceId;

    private String description;

    @NotBlank(message = "La direccion de la visita es obligatoria")
    private String addressLine;

    @NotBlank(message = "La ciudad es obligatoria")
    private String city;

    @NotNull(message = "La fecha de la visita es obligatoria")
    @Future(message = "La fecha de la visita debe ser posterior a la actual")
    private LocalDateTime scheduledAt;

    @NotNull(message = "El metodo de pago es obligatorio")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Indica que tan pronto necesitas el servicio")
    private BookingUrgency urgency;
}
