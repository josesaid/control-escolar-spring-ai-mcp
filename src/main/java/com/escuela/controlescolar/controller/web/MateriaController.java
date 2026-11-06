package com.escuela.controlescolar.controller.web;

import com.escuela.controlescolar.model.Materia;
import com.escuela.controlescolar.service.MaestroService;
import com.escuela.controlescolar.service.MateriaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador web MVC para la gestión de materias.
 */
@Controller
@RequestMapping("/materias")
public class MateriaController {

    private final MateriaService materiaService;
    private final MaestroService maestroService;

    public MateriaController(MateriaService materiaService, MaestroService maestroService) {
        this.materiaService = materiaService;
        this.maestroService = maestroService;
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MAESTRO','SECRETARIA','ALUMNO')")
    public String listar(Model model) {
        model.addAttribute("materias", materiaService.listarTodos());
        return "materias/list";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String nuevo(Model model) {
        model.addAttribute("materia", new Materia());
        model.addAttribute("maestros", maestroService.listarTodos());
        return "materias/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String guardar(@ModelAttribute Materia materia) {
        materiaService.guardar(materia);
        return "redirect:/materias";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String editar(@PathVariable Long id, Model model) {
        Materia materia = materiaService.buscarPorId(id);
        if (materia == null) {
            return "redirect:/materias";
        }
        model.addAttribute("materia", materia);
        model.addAttribute("maestros", maestroService.listarTodos());
        return "materias/form";
    }

    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String eliminar(@PathVariable Long id) {
        materiaService.eliminar(id);
        return "redirect:/materias";
    }
}
