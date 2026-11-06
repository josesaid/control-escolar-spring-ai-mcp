package com.escuela.controlescolar.controller.web;

import com.escuela.controlescolar.model.Aula;
import com.escuela.controlescolar.service.AulaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador web MVC para la gestión de aulas.
 */
@Controller
@RequestMapping("/aulas")
public class AulaController {

    private final AulaService aulaService;

    public AulaController(AulaService aulaService) {
        this.aulaService = aulaService;
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MAESTRO','SECRETARIA','ALUMNO')")
    public String listar(Model model) {
        model.addAttribute("aulas", aulaService.listarTodos());
        return "aulas/list";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String nuevo(Model model) {
        model.addAttribute("aula", new Aula());
        return "aulas/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String guardar(@ModelAttribute Aula aula) {
        aulaService.guardar(aula);
        return "redirect:/aulas";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String editar(@PathVariable Long id, Model model) {
        Aula aula = aulaService.buscarPorId(id);
        if (aula == null) {
            return "redirect:/aulas";
        }
        model.addAttribute("aula", aula);
        return "aulas/form";
    }

    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String eliminar(@PathVariable Long id) {
        aulaService.eliminar(id);
        return "redirect:/aulas";
    }
}
