package com.nexjob.platform.dto.request;

import com.nexjob.platform.enums.IntakeFieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CategoryIntakeFieldRequest {

    // Opcional: si no se manda (pregunta nueva agregada en el editor), el backend genera uno.
    private String id;

    @NotBlank(message = "El texto de la pregunta es obligatorio")
    private String label;

    @NotNull(message = "El tipo de pregunta es obligatorio")
    private IntakeFieldType type;

    // Solo se usa cuando type = SELECT; se valida que no venga vacio en ese caso.
    private List<String> options;

    // Id de un campo tipo CHECKBOX de esta misma categoria: si se manda, este campo solo se
    // muestra al cliente cuando esa casilla esta marcada. Null/vacio = siempre visible.
    // Mutuamente excluyente con showIfFieldId/showIfValue.
    private String showIfChecked;

    // Version general: este campo solo se muestra si la respuesta del campo showIfFieldId es
    // exactamente showIfValue. Mutuamente excluyente con showIfChecked.
    private String showIfFieldId;
    private String showIfValue;

    // Solo se usa cuando type = DIMENSIONS: "cm" o "m".
    private String unit;
}
