package com.escuela.controlescolar.repository;

import com.escuela.controlescolar.model.Horario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Long> {
    List<Horario> findByGrupoId(Long grupoId);
    List<Horario> findByMaestroId(Long maestroId);
}
