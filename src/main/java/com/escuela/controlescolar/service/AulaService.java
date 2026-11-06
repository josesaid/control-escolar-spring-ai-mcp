package com.escuela.controlescolar.service;

import com.escuela.controlescolar.model.Aula;
import com.escuela.controlescolar.repository.AulaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AulaService {

    private final AulaRepository aulaRepository;

    public AulaService(AulaRepository aulaRepository) {
        this.aulaRepository = aulaRepository;
    }

    public List<Aula> listarTodos() {
        return aulaRepository.findAll();
    }

    public Aula buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        Optional<Aula> aula = aulaRepository.findById(id);
        return aula.orElse(null);
    }

    @Transactional
    public Aula guardar(Aula aula) {
        return aulaRepository.save(aula);
    }

    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            return;
        }
        aulaRepository.deleteById(id);
    }

    public long contar() {
        return aulaRepository.count();
    }
}
