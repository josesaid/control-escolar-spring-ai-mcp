package com.escuela.controlescolar.repository;

import com.escuela.controlescolar.model.Maestro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaestroRepository extends JpaRepository<Maestro, Long> {
    Optional<Maestro> findByNumeroEmpleado(String numeroEmpleado);
}
