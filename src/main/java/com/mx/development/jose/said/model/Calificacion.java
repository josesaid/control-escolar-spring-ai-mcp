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
@Table(name = "calificaciones")
public class Calificacion {

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
    private String periodo;

    @Column
    private Double valor;

    @Column(name = "tipo_evaluacion")
    private String tipoEvaluacion;

    @Column
    private LocalDate fecha;

    @Column
    private String observaciones;

    public Calificacion() {
    }

    public Calificacion(Alumno alumno, Materia materia, String periodo, Double valor, String tipoEvaluacion, LocalDate fecha, String observaciones) {
        this.alumno = alumno;
        this.materia = materia;
        this.periodo = periodo;
        this.valor = valor;
        this.tipoEvaluacion = tipoEvaluacion;
        this.fecha = fecha;
        this.observaciones = observaciones;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }
    public Materia getMateria() { return materia; }
    public void setMateria(Materia materia) { this.materia = materia; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }
    public String getTipoEvaluacion() { return tipoEvaluacion; }
    public void setTipoEvaluacion(String tipoEvaluacion) { this.tipoEvaluacion = tipoEvaluacion; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
