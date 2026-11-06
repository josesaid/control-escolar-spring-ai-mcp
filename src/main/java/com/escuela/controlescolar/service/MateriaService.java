package com.escuela.controlescolar.service;

import com.escuela.controlescolar.model.Materia;
import com.escuela.controlescolar.repository.MateriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MateriaService {

    private final MateriaRepository materiaRepository;

    public MateriaService(MateriaRepository materiaRepository) {
        this.materiaRepository = materiaRepository;
    }

    public List<Materia> listarTodos() {
        return materiaRepository.findAll();
    }

    public Materia buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        Optional<Materia> materia = materiaRepository.findById(id);
        return materia.orElse(null);
    }

    public Materia buscarPorClave(String clave) {
        if (clave == null || clave.isBlank()) {
            return null;
        }
        Optional<Materia> materia = materiaRepository.findByClave(clave);
        return materia.orElse(null);
    }

    public List<Materia> listarPorMaestro(Long maestroId) {
        if (maestroId == null) {
            return List.of();
        }
        return materiaRepository.findByMaestroId(maestroId);
    }

    @Transactional
    public Materia guardar(Materia materia) {
        return materiaRepository.save(materia);
    }

    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            return;
        }
        materiaRepository.deleteById(id);
    }

    public long contar() {
        return materiaRepository.count();
    }
}
