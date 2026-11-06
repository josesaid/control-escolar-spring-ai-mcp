package com.mx.development.jose.said.model;

public enum Permiso {
    LEER_ALUMNOS("Leer alumnos"),
    CREAR_ALUMNOS("Crear alumnos"),
    EDITAR_ALUMNOS("Editar alumnos"),
    ELIMINAR_ALUMNOS("Eliminar alumnos"),
    LEER_CALIFICACIONES("Leer calificaciones"),
    EDITAR_CALIFICACIONES("Editar calificaciones"),
    LEER_REPORTES("Leer reportes"),
    GESTION_USUARIOS("Gestión de usuarios"),
    GESTION_HORARIOS("Gestión de horarios"),
    ASISTENTE_IA("Usar asistente de IA");

    private final String descripcion;

    Permiso(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
