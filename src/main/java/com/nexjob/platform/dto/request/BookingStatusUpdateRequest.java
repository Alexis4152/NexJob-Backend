package com.nexjob.platform.dto.request;

import com.nexjob.platform.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Usado por el prestador para mover una tarjeta del tablero Kanban a otra columna/estado. */
@Data
public class BookingStatusUpdateRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private BookingStatus newStatus;

    private String note;
}
