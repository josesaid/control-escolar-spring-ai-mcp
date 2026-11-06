package com.mx.development.jose.said.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "nombre_completo")
    private String nombreCompleto;

    @Column
    private String email;

    @Enumerated(EnumType.STRING)
    @Column
    private Rol rol;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_permisos", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permiso")
    private Set<Permiso> permisos = new HashSet<>();

    @Column
    private boolean activo = true;

    public Usuario() {
    }

    public Usuario(String username, String password, String nombreCompleto, String email, Rol rol, Set<Permiso> permisos, boolean activo) {
        this.username = username;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.rol = rol;
        this.permisos = permisos;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public Set<Permiso> getPermisos() { return permisos; }
    public void setPermisos(Set<Permiso> permisos) { this.permisos = permisos; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
