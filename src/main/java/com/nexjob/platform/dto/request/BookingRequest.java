package com.nexjob.platform.dto.request;

import com.nexjob.platform.enums.BookingUrgency;
import com.nexjob.platform.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Solicitud de contratacion de un servicio. Direccion, ciudad y fecha de la visita son
 * obligatorias solo para servicios de precio fijo/por hora: para un servicio "a cotizar" no
 * existe visita que agendar todavia (ver BookingServiceImpl.create, que aplica esa validacion
 * segun el tipo de precio del servicio, no aqui, porque este DTO no conoce ese dato).
 */
@Data
public class BookingRequest {

    @NotNull(message = "El servicio es obligatorio")
    private Long serviceId;

    private String description;

    private String addressLine;

    private String city;

    private LocalDateTime scheduledAt;

    @NotNull(message = "El metodo de pago es obligatorio")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Indica que tan pronto necesitas el servicio")
    private BookingUrgency urgency;
}
