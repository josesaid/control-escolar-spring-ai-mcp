package com.escuela.controlescolar.controller.web;

import com.escuela.controlescolar.model.Calificacion;
import com.escuela.controlescolar.service.AlumnoService;
import com.escuela.controlescolar.service.CalificacionService;
import com.escuela.controlescolar.service.MateriaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador web MVC para la gestión de calificaciones.
 */
@Controller
@RequestMapping("/calificaciones")
public class CalificacionController {

    private final CalificacionService calificacionService;
    private final AlumnoService alumnoService;
    private final MateriaService materiaService;

    public CalificacionController(CalificacionService calificacionService,
                                  AlumnoService alumnoService,
                                  MateriaService materiaService) {
        this.calificacionService = calificacionService;
        this.alumnoService = alumnoService;
        this.materiaService = materiaService;
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MAESTRO','SECRETARIA')")
    public String listar(@RequestParam(required = false) Long alumnoId, Model model) {
        if (alumnoId != null) {
            model.addAttribute("calificaciones", calificacionService.listarPorAlumno(alumnoId));
            model.addAttribute("promedioAlumno", calificacionService.promedioPorAlumno(alumnoId));
            model.addAttribute("alumnoId", alumnoId);
        } else {
            model.addAttribute("calificaciones", calificacionService.listarTodas());
        }
        model.addAttribute("alumnos", alumnoService.listarTodos());
        return "calificaciones/list";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MAESTRO')")
    public String nuevo(Model model) {
        model.addAttribute("calificacion", new Calificacion());
        model.addAttribute("alumnos", alumnoService.listarTodos());
        model.addAttribute("materias", materiaService.listarTodos());
        return "calificaciones/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MAESTRO')")
    public String guardar(@ModelAttribute Calificacion calificacion) {
        calificacionService.guardar(calificacion);
        return "redirect:/calificaciones";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MAESTRO')")
    public String editar(@PathVariable Long id, Model model) {
        Calificacion calificacion = calificacionService.buscarPorId(id);
        if (calificacion == null) {
            return "redirect:/calificaciones";
        }
        model.addAttribute("calificacion", calificacion);
        model.addAttribute("alumnos", alumnoService.listarTodos());
        model.addAttribute("materias", materiaService.listarTodos());
        return "calificaciones/form";
    }

    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MAESTRO')")
    public String eliminar(@PathVariable Long id) {
        calificacionService.eliminar(id);
        return "redirect:/calificaciones";
    }
}
