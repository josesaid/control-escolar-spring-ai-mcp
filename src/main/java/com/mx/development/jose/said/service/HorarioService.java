package com.mx.development.jose.said.service;

import com.mx.development.jose.said.model.Horario;
import com.mx.development.jose.said.repository.HorarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class HorarioService {

    private final HorarioRepository horarioRepository;

    public HorarioService(HorarioRepository horarioRepository) {
        this.horarioRepository = horarioRepository;
    }

    public List<Horario> listarTodos() {
        return horarioRepository.findAll();
    }

    public Horario buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        Optional<Horario> horario = horarioRepository.findById(id);
        return horario.orElse(null);
    }

    public List<Horario> listarPorGrupo(Long grupoId) {
        if (grupoId == null) {
            return List.of();
        }
        return horarioRepository.findByGrupoId(grupoId);
    }

    public List<Horario> listarPorMaestro(Long maestroId) {
        if (maestroId == null) {
            return List.of();
        }
        return horarioRepository.findByMaestroId(maestroId);
    }

    @Transactional
    public Horario guardar(Horario horario) {
        return horarioRepository.save(horario);
    }

    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            return;
        }
        horarioRepository.deleteById(id);
    }

    public long contar() {
        return horarioRepository.count();
    }
}
