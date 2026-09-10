package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Conteos por columna del tablero Kanban del prestador (ver ProviderBookingController). */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProviderDashboardResponse {
    private long solicitados;
    private long porHacer;
    private long enProceso;
    private long concluidos;
    private List<BookingSummaryResponse> proximasVisitas;
}
