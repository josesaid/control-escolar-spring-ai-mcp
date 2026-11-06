package com.escuela.controlescolar.service;

import com.escuela.controlescolar.model.Maestro;
import com.escuela.controlescolar.repository.MaestroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MaestroService {

    private final MaestroRepository maestroRepository;

    public MaestroService(MaestroRepository maestroRepository) {
        this.maestroRepository = maestroRepository;
    }

    public List<Maestro> listarTodos() {
        return maestroRepository.findAll();
    }

    public Maestro buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        Optional<Maestro> maestro = maestroRepository.findById(id);
        return maestro.orElse(null);
    }

    public Maestro buscarPorNumeroEmpleado(String numeroEmpleado) {
        if (numeroEmpleado == null || numeroEmpleado.isBlank()) {
            return null;
        }
        Optional<Maestro> maestro = maestroRepository.findByNumeroEmpleado(numeroEmpleado);
        return maestro.orElse(null);
    }

    @Transactional
    public Maestro guardar(Maestro maestro) {
        return maestroRepository.save(maestro);
    }

    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            return;
        }
        maestroRepository.deleteById(id);
    }

    public long contar() {
        return maestroRepository.count();
    }
}
