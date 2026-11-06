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
@Table(name = "asistencias")
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;

    @ManyToOne
    @JoinColumn(name = "materia_id")
    private Materia materia;

    @Column
    private LocalDate fecha;

    @Column
    private Boolean presente;

    @Column
    private String observaciones;

    public Asistencia() {
    }

    public Asistencia(Alumno alumno, Materia materia, LocalDate fecha, Boolean presente, String observaciones) {
        this.alumno = alumno;
        this.materia = materia;
        this.fecha = fecha;
        this.presente = presente;
        this.observaciones = observaciones;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }
    public Materia getMateria() { return materia; }
    public void setMateria(Materia materia) { this.materia = materia; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Boolean getPresente() { return presente; }
    public void setPresente(Boolean presente) { this.presente = presente; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
