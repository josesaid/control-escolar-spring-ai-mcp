package com.mx.development.jose.said.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "materias")
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String clave;

    @Column
    private String nombre;

    @Column
    private String descripcion;

    @Column
    private Integer creditos;

    @Column(name = "horas_semana")
    private Integer horasSemana;

    @ManyToOne
    @JoinColumn(name = "maestro_id")
    private Maestro maestro;

    public Materia() {
    }

    public Materia(String clave, String nombre, String descripcion, Integer creditos, Integer horasSemana, Maestro maestro) {
        this.clave = clave;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.creditos = creditos;
        this.horasSemana = horasSemana;
        this.maestro = maestro;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Integer getCreditos() { return creditos; }
    public void setCreditos(Integer creditos) { this.creditos = creditos; }
    public Integer getHorasSemana() { return horasSemana; }
    public void setHorasSemana(Integer horasSemana) { this.horasSemana = horasSemana; }
    public Maestro getMaestro() { return maestro; }
    public void setMaestro(Maestro maestro) { this.maestro = maestro; }
}
