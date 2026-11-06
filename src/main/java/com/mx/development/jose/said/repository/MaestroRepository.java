package com.mx.development.jose.said.repository;

import com.mx.development.jose.said.model.Maestro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaestroRepository extends JpaRepository<Maestro, Long> {
    Optional<Maestro> findByNumeroEmpleado(String numeroEmpleado);
}
