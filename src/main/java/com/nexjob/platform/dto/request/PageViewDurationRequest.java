package com.nexjob.platform.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PageViewDurationRequest {

    @NotNull(message = "La duracion es obligatoria")
    @Min(value = 0, message = "La duracion no puede ser negativa")
    private Integer durationSeconds;
}
