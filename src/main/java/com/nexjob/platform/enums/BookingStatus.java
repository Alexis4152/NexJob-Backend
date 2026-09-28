package com.nexjob.platform.enums;

import java.util.Set;

/**
 * Estados del ciclo de vida de una contratacion. El tablero Kanban del prestador
 * ({@code GET /api/provider/bookings}) agrupa estas columnas: SOLICITADO ("Solicitudes"),
 * ACEPTADO ("Por hacer"), EN_PROCESO ("En proceso") y CONCLUIDO/APROBADO ("Concluidos").
 */
public enum BookingStatus {
    SOLICITADO,
    // Solo para servicios "a cotizar" (ver ServiceOffering.priceType): el prestador ya envio su
    // desglose de materiales/costos y se espera la respuesta del cliente (aceptar o rechazar).
    COTIZADO,
    // El cliente acepto la cotizacion (precio ya acordado) pero el prestador todavia debe
    // confirmar para agendar la visita; una vez confirma, pasa a ACEPTADO igual que un servicio
    // de precio fijo.
    COTIZACION_ACEPTADA,
    ACEPTADO,
    EN_PROCESO,
    CONCLUIDO,
    APROBADO,
    RECHAZADO,
    CANCELADO;

    /** Estados que liberan el horario agendado: ya no bloquean el calendario del prestador. */
    public static final Set<BookingStatus> SLOT_RELEASED = Set.of(RECHAZADO, CANCELADO);
}
