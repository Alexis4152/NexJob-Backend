package com.nexjob.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
}
