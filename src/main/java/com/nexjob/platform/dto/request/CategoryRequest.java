package com.nexjob.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String description;

    private String icon;
}
