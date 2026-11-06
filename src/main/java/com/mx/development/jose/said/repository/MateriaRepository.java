package com.mx.development.jose.said.repository;

import com.mx.development.jose.said.model.Materia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MateriaRepository extends JpaRepository<Materia, Long> {
    Optional<Materia> findByClave(String clave);
    List<Materia> findByMaestroId(Long maestroId);
}
