package com.nexjob.platform.entity;

import com.nexjob.platform.enums.IntakeFieldType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Una pregunta especifica de una {@link Category} (ver plan "Cotizacion a la Medida"): el
 * cliente las responde al contratar un servicio de esa categoria, ademas de su descripcion
 * libre, para que el prestador reciba una ficha clara (medidas, materiales, zona...) en vez de
 * una frase ambigua. No es una entidad JPA propia: la lista completa se guarda como JSON en
 * {@code categories.intake_fields_json} (ver CategoryIntakeFieldsConverter).
 */
@Data
@NoArgsConstructor
public class CategoryIntakeField {
    private String id;
    private String label;
    private IntakeFieldType type;
    /** Solo aplica cuando type = SELECT o MULTISELECT. */
    private List<String> options;
    /** Solo aplica cuando type = DIMENSIONS: "cm" (default, muebles) o "m" (habitaciones,
     * fachadas). Null se trata como "cm" para no romper preguntas ya guardadas. */
    private String unit;
    /** Si se define, este campo solo se muestra al cliente cuando el campo tipo CHECKBOX con
     * este id esta marcado (ver "Instalacion" en Carpinteria: agrupa varias preguntas detras de
     * un interruptor en vez de mostrarlas todas de golpe). Null/vacio = siempre visible.
     * Mutuamente excluyente con showIfFieldId/showIfValue. */
    private String showIfChecked;
    /** Version mas general de showIfChecked: este campo solo se muestra si la respuesta del
     * campo con id showIfFieldId es exactamente showIfValue (ej. "cuantas habitaciones" solo
     * aparece si "Tipo de servicio" = "Pintura de casa completa"). Para MULTISELECT, se muestra
     * si showIfValue esta entre las opciones marcadas. Mutuamente excluyente con showIfChecked. */
    private String showIfFieldId;
    private String showIfValue;
}
