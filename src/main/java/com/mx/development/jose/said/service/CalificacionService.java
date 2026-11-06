package com.mx.development.jose.said.service;

import com.mx.development.jose.said.model.Calificacion;
import com.mx.development.jose.said.repository.CalificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;

    public CalificacionService(CalificacionRepository calificacionRepository) {
        this.calificacionRepository = calificacionRepository;
    }

    public List<Calificacion> listarTodas() {
        return calificacionRepository.findAll();
    }

    public Calificacion buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        Optional<Calificacion> calificacion = calificacionRepository.findById(id);
        return calificacion.orElse(null);
    }

    public List<Calificacion> listarPorAlumno(Long alumnoId) {
        if (alumnoId == null) {
            return List.of();
        }
        return calificacionRepository.findByAlumnoId(alumnoId);
    }

    public List<Calificacion> listarPorMateria(Long materiaId) {
        if (materiaId == null) {
            return List.of();
        }
        return calificacionRepository.findByMateriaId(materiaId);
    }

    public List<Calificacion> listarPorAlumnoYPeriodo(Long alumnoId, String periodo) {
        if (alumnoId == null || periodo == null || periodo.isBlank()) {
            return List.of();
        }
        return calificacionRepository.findByAlumnoIdAndPeriodo(alumnoId, periodo);
    }

    @Transactional
    public Calificacion guardar(Calificacion calificacion) {
        return calificacionRepository.save(calificacion);
    }

    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            return;
        }
        calificacionRepository.deleteById(id);
    }

    public Double promedioPorAlumno(Long alumnoId) {
        if (alumnoId == null) {
            return 0.0;
        }
        return promedioDe(calificacionRepository.findByAlumnoId(alumnoId));
    }

    public Double promedioPorMateria(Long materiaId) {
        if (materiaId == null) {
            return 0.0;
        }
        return promedioDe(calificacionRepository.findByMateriaId(materiaId));
    }

    public Double promedioGeneral() {
        return promedioDe(calificacionRepository.findAll());
    }

    private Double promedioDe(List<Calificacion> calificaciones) {
        if (calificaciones == null || calificaciones.isEmpty()) {
            return 0.0;
        }
        double suma = 0.0;
        int total = 0;
        for (Calificacion calificacion : calificaciones) {
            if (calificacion != null && calificacion.getValor() != null) {
                suma += calificacion.getValor();
                total++;
            }
        }
        if (total == 0) {
            return 0.0;
        }
        return suma / total;
    }
}
