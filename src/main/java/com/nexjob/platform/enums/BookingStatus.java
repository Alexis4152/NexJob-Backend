package com.nexjob.platform.enums;

import java.util.Set;

/**
 * Estados del ciclo de vida de una contratacion. El tablero Kanban del prestador
 * ({@code GET /api/provider/bookings}) agrupa estas columnas: SOLICITADO ("Solicitudes"),
 * ACEPTADO ("Por hacer"), EN_PROCESO ("En proceso") y CONCLUIDO/APROBADO ("Concluidos").
 */
public enum BookingStatus {
    SOLICITADO,
    ACEPTADO,
    EN_PROCESO,
    CONCLUIDO,
    APROBADO,
    RECHAZADO,
    CANCELADO;

    /** Estados que liberan el horario agendado: ya no bloquean el calendario del prestador. */
    public static final Set<BookingStatus> SLOT_RELEASED = Set.of(RECHAZADO, CANCELADO);
}
