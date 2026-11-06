package com.mx.development.jose.said.controller.api;

import com.mx.development.jose.said.model.Alumno;
import com.mx.development.jose.said.service.AlumnoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST para alumnos.
 */
@RestController
@RequestMapping("/api/alumnos")
public class ApiAlumnoController {

    private final AlumnoService alumnoService;

    public ApiAlumnoController(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }

    @GetMapping("")
    public List<Alumno> listar() {
        return alumnoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alumno> buscarPorId(@PathVariable Long id) {
        Alumno alumno = alumnoService.buscarPorId(id);
        if (alumno == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(alumno);
    }

    @GetMapping("/matricula/{matricula}")
    public ResponseEntity<Alumno> buscarPorMatricula(@PathVariable String matricula) {
        Alumno alumno = alumnoService.buscarPorMatricula(matricula);
        if (alumno == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(alumno);
    }

    @GetMapping("/grupo/{grupoId}")
    public List<Alumno> listarPorGrupo(@PathVariable Long grupoId) {
        return alumnoService.listarPorGrupo(grupoId);
    }
}
