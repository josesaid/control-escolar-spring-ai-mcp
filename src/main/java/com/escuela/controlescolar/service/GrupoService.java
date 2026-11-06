package com.escuela.controlescolar.service;

import com.escuela.controlescolar.model.Grupo;
import com.escuela.controlescolar.repository.GrupoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GrupoService {

    private final GrupoRepository grupoRepository;

    public GrupoService(GrupoRepository grupoRepository) {
        this.grupoRepository = grupoRepository;
    }

    public List<Grupo> listarTodos() {
        return grupoRepository.findAll();
    }

    public Grupo buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        Optional<Grupo> grupo = grupoRepository.findById(id);
        return grupo.orElse(null);
    }

    public Grupo buscarPorClave(String clave) {
        if (clave == null || clave.isBlank()) {
            return null;
        }
        Optional<Grupo> grupo = grupoRepository.findByClave(clave);
        return grupo.orElse(null);
    }

    @Transactional
    public Grupo guardar(Grupo grupo) {
        return grupoRepository.save(grupo);
    }

    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            return;
        }
        grupoRepository.deleteById(id);
    }

    public long contar() {
        return grupoRepository.count();
    }
}
