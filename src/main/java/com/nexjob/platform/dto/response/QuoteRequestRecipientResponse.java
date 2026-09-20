package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Estado de uno de los prestadores a los que se les envio la solicitud. */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class QuoteRequestRecipientResponse {
    private Long providerId;
    private String providerBusinessName;
    /** "PENDIENTE" | "COTIZO" | "DESCARTADO" */
    private String status;
}
