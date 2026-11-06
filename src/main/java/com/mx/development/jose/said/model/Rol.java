package com.mx.development.jose.said.model;

public enum Rol {
    ADMIN("Administrador del sistema"),
    DIRECTOR("Director de la escuela"),
    MAESTRO("Maestro / Docente"),
    ALUMNO("Alumno / Estudiante"),
    SECRETARIA("Secretaria / Personal administrativo");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
