package com.mx.development.jose.said.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "inscripciones")
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @ManyToOne
    @JoinColumn(name = "materia_id")
    private Materia materia;

    @Column
    private String periodo;

    @Column(name = "fecha_inscripcion")
    private LocalDate fechaInscripcion;

    public Inscripcion() {
    }

    public Inscripcion(Alumno alumno, Grupo grupo, Materia materia, String periodo, LocalDate fechaInscripcion) {
        this.alumno = alumno;
        this.grupo = grupo;
        this.materia = materia;
        this.periodo = periodo;
        this.fechaInscripcion = fechaInscripcion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }
    public Grupo getGrupo() { return grupo; }
    public void setGrupo(Grupo grupo) { this.grupo = grupo; }
    public Materia getMateria() { return materia; }
    public void setMateria(Materia materia) { this.materia = materia; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public LocalDate getFechaInscripcion() { return fechaInscripcion; }
    public void setFechaInscripcion(LocalDate fechaInscripcion) { this.fechaInscripcion = fechaInscripcion; }
}
