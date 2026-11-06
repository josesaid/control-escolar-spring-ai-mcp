package com.mx.development.jose.said.service;

import com.mx.development.jose.said.model.Alumno;
import com.mx.development.jose.said.model.Asistencia;
import com.mx.development.jose.said.repository.AsistenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;

    public AsistenciaService(AsistenciaRepository asistenciaRepository) {
        this.asistenciaRepository = asistenciaRepository;
    }

    public List<Asistencia> listarTodas() {
        return asistenciaRepository.findAll();
    }

    public List<Asistencia> listarPorAlumno(Long alumnoId) {
        if (alumnoId == null) {
            return List.of();
        }
        return asistenciaRepository.findByAlumnoId(alumnoId);
    }

    @Transactional
    public Asistencia guardar(Asistencia asistencia) {
        return asistenciaRepository.save(asistencia);
    }

    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            return;
        }
        asistenciaRepository.deleteById(id);
    }

    /**
     * Porcentaje de asistencias presentes sobre el total de registros
     * del alumno. Devuelve 0.0 si no hay registros.
     */
    public Double porcentajeAsistenciaPorAlumno(Long alumnoId) {
        if (alumnoId == null) {
            return 0.0;
        }
        List<Asistencia> registros = asistenciaRepository.findByAlumnoId(alumnoId);
        if (registros == null || registros.isEmpty()) {
            return 0.0;
        }
        long presentes = registros.stream()
                .filter(a -> a != null && Boolean.TRUE.equals(a.getPresente()))
                .count();
        return (presentes * 100.0) / registros.size();
    }
}
