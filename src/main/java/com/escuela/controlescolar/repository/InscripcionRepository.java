package com.escuela.controlescolar.repository;

import com.escuela.controlescolar.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    List<Inscripcion> findByAlumnoId(Long alumnoId);
}
