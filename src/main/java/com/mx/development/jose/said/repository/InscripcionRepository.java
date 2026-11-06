package com.mx.development.jose.said.repository;

import com.mx.development.jose.said.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    List<Inscripcion> findByAlumnoId(Long alumnoId);
}
