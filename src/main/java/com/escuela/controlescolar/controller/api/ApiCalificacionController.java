package com.escuela.controlescolar.controller.api;

import com.escuela.controlescolar.model.Calificacion;
import com.escuela.controlescolar.service.CalificacionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API REST para calificaciones y promedios.
 */
@RestController
@RequestMapping("/api/calificaciones")
public class ApiCalificacionController {

    private final CalificacionService calificacionService;

    public ApiCalificacionController(CalificacionService calificacionService) {
        this.calificacionService = calificacionService;
    }

    @GetMapping("")
    public List<Calificacion> listar() {
        return calificacionService.listarTodas();
    }

    @GetMapping("/alumno/{alumnoId}")
    public List<Calificacion> listarPorAlumno(@PathVariable Long alumnoId) {
        return calificacionService.listarPorAlumno(alumnoId);
    }

    @GetMapping("/alumno/{alumnoId}/promedio")
    public Map<String, Object> promedioPorAlumno(@PathVariable Long alumnoId) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("alumnoId", alumnoId);
        resultado.put("promedio", calificacionService.promedioPorAlumno(alumnoId));
        return resultado;
    }

    @GetMapping("/materia/{materiaId}/promedio")
    public Map<String, Object> promedioPorMateria(@PathVariable Long materiaId) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("materiaId", materiaId);
        resultado.put("promedio", calificacionService.promedioPorMateria(materiaId));
        return resultado;
    }
}
