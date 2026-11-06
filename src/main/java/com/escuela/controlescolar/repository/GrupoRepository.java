package com.escuela.controlescolar.repository;

import com.escuela.controlescolar.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    Optional<Grupo> findByClave(String clave);
}
