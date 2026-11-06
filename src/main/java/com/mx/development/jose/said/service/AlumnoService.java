package com.mx.development.jose.said.service;

import com.mx.development.jose.said.model.Alumno;
import com.mx.development.jose.said.repository.AlumnoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AlumnoService {

    private final AlumnoRepository alumnoRepository;

    public AlumnoService(AlumnoRepository alumnoRepository) {
        this.alumnoRepository = alumnoRepository;
    }

    public List<Alumno> listarTodos() {
        return alumnoRepository.findAll();
    }

    public List<Alumno> listarActivos() {
        return alumnoRepository.findByActivoTrue();
    }

    public Alumno buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        Optional<Alumno> alumno = alumnoRepository.findById(id);
        return alumno.orElse(null);
    }

    public Alumno buscarPorMatricula(String matricula) {
        if (matricula == null || matricula.isBlank()) {
            return null;
        }
        Optional<Alumno> alumno = alumnoRepository.findByMatricula(matricula);
        return alumno.orElse(null);
    }

    public List<Alumno> listarPorGrupo(Long grupoId) {
        if (grupoId == null) {
            return List.of();
        }
        return alumnoRepository.findByGrupoId(grupoId);
    }

    @Transactional
    public Alumno guardar(Alumno alumno) {
        return alumnoRepository.save(alumno);
    }

    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            return;
        }
        alumnoRepository.deleteById(id);
    }

    public long contar() {
        return alumnoRepository.count();
    }
}
