package com.mx.development.jose.said.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalTime;

@Entity
@Table(name = "horarios")
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "materia_id")
    private Materia materia;

    @ManyToOne
    @JoinColumn(name = "maestro_id")
    private Maestro maestro;

    @ManyToOne
    @JoinColumn(name = "aula_id")
    private Aula aula;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @Column(name = "dia_semana")
    private String diaSemana;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    public Horario() {
    }

    public Horario(Materia materia, Maestro maestro, Aula aula, Grupo grupo, String diaSemana, LocalTime horaInicio, LocalTime horaFin) {
        this.materia = materia;
        this.maestro = maestro;
        this.aula = aula;
        this.grupo = grupo;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Materia getMateria() { return materia; }
    public void setMateria(Materia materia) { this.materia = materia; }
    public Maestro getMaestro() { return maestro; }
    public void setMaestro(Maestro maestro) { this.maestro = maestro; }
    public Aula getAula() { return aula; }
    public void setAula(Aula aula) { this.aula = aula; }
    public Grupo getGrupo() { return grupo; }
    public void setGrupo(Grupo grupo) { this.grupo = grupo; }
    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
}
