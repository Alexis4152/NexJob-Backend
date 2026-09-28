package com.nexjob.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class Category extends AuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(length = 500)
    private String description;

    /** Emoji o nombre de icono corto para mostrar en tarjetas de categoria en el frontend. */
    @Column(length = 10)
    private String icon;

    // Preguntas especificas que el cliente responde al contratar un servicio de esta categoria
    // (ver "Cotizacion a la Medida"), ademas de su descripcion libre. Vacia = sin cuestionario
    // definido, el formulario del cliente se comporta igual que antes de esta funcionalidad.
    @Convert(converter = CategoryIntakeFieldsConverter.class)
    @Column(name = "intake_fields_json", columnDefinition = "TEXT")
    private List<CategoryIntakeField> intakeFields = new ArrayList<>();
}
