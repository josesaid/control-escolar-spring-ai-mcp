package com.mx.development.jose.said.repository;

import com.mx.development.jose.said.model.Horario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Long> {
    List<Horario> findByGrupoId(Long grupoId);
    List<Horario> findByMaestroId(Long maestroId);
}
