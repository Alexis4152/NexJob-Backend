package com.nexjob.platform.enums;

/** Tipo de campo de un cuestionario de categoria (ver Category.intakeFields). */
public enum IntakeFieldType {
    SELECT,
    // Como SELECT pero el cliente puede marcar varias opciones a la vez (ej. "que espacios de
    // la casa" en Pintura); la respuesta es una lista, no un solo valor.
    MULTISELECT,
    NUMBER,
    TEXT,
    CHECKBOX,
    DIMENSIONS
}
