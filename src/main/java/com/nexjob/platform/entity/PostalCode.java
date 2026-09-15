package com.nexjob.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Catalogo de referencia de codigos postales de Mexico (GeoNames/SEPOMEX). Ver
 * db/06_postal_codes.sql: es de solo lectura desde la aplicacion, se recarga completo
 * desde el archivo fuente cuando hace falta, nunca se edita fila por fila.
 */
@Entity
@Table(name = "postal_codes")
@Getter @Setter @NoArgsConstructor
public class PostalCode {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "country_code", length = 2, nullable = false)
    private String countryCode;

    @Column(name = "postal_code", length = 10, nullable = false)
    private String postalCode;

    @Column(name = "place_name", length = 180)
    private String placeName;

    @Column(name = "admin_name1", length = 100)
    private String adminName1;

    @Column(name = "admin_code1", length = 20)
    private String adminCode1;

    @Column(name = "admin_name2", length = 100)
    private String adminName2;

    @Column(name = "admin_code2", length = 20)
    private String adminCode2;

    @Column(name = "admin_name3", length = 100)
    private String adminName3;

    @Column(name = "admin_code3", length = 20)
    private String adminCode3;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    private Short accuracy;
}
