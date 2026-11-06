package com.mx.development.jose.said.controller.web;

import com.mx.development.jose.said.model.Alumno;
import com.mx.development.jose.said.service.AlumnoService;
import com.mx.development.jose.said.service.GrupoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador web MVC para la gestión de alumnos.
 */
@Controller
@RequestMapping("/alumnos")
public class AlumnoController {

    private final AlumnoService alumnoService;
    private final GrupoService grupoService;

    public AlumnoController(AlumnoService alumnoService, GrupoService grupoService) {
        this.alumnoService = alumnoService;
        this.grupoService = grupoService;
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MAESTRO','SECRETARIA','ALUMNO')")
    public String listar(Model model) {
        model.addAttribute("alumnos", alumnoService.listarTodos());
        return "alumnos/list";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String nuevo(Model model) {
        model.addAttribute("alumno", new Alumno());
        model.addAttribute("grupos", grupoService.listarTodos());
        return "alumnos/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String guardar(@ModelAttribute Alumno alumno) {
        alumnoService.guardar(alumno);
        return "redirect:/alumnos";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String editar(@PathVariable Long id, Model model) {
        Alumno alumno = alumnoService.buscarPorId(id);
        if (alumno == null) {
            return "redirect:/alumnos";
        }
        model.addAttribute("alumno", alumno);
        model.addAttribute("grupos", grupoService.listarTodos());
        return "alumnos/form";
    }

    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String eliminar(@PathVariable Long id) {
        alumnoService.eliminar(id);
        return "redirect:/alumnos";
    }
}
