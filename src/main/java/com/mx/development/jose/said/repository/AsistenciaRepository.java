package com.mx.development.jose.said.repository;

import com.mx.development.jose.said.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    List<Asistencia> findByAlumnoId(Long alumnoId);
}
