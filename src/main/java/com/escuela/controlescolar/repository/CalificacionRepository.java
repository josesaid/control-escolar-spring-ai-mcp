package com.escuela.controlescolar.repository;

import com.escuela.controlescolar.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    List<Calificacion> findByAlumnoId(Long alumnoId);
    List<Calificacion> findByMateriaId(Long materiaId);
    List<Calificacion> findByAlumnoIdAndPeriodo(Long alumnoId, String periodo);
    List<Calificacion> findByAlumnoIdAndMateriaId(Long alumnoId, Long materiaId);
}
