package com.mx.development.jose.said.repository;

import com.mx.development.jose.said.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {
    Optional<Alumno> findByMatricula(String matricula);
    List<Alumno> findByGrupoId(Long grupoId);
    List<Alumno> findByActivoTrue();
}
