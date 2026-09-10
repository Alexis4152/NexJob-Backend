package com.nexjob.platform.dto.request;

import com.nexjob.platform.enums.TicketCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Modulo de ayuda: reportar un problema o dejar una queja/sugerencia sobre la plataforma. */
@Data
public class SupportTicketRequest {

    @NotBlank(message = "El correo de contacto es obligatorio")
    @Email(message = "El correo no es valido")
    private String contactEmail;

    @NotBlank(message = "El asunto es obligatorio")
    private String subject;

    @NotNull(message = "La categoria es obligatoria")
    private TicketCategory category;

    @NotBlank(message = "Describe el problema o comentario")
    private String message;
}
