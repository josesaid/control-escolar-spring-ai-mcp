package com.mx.development.jose.said.controller.web;

import com.mx.development.jose.said.model.Maestro;
import com.mx.development.jose.said.service.MaestroService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador web MVC para la gestión de maestros.
 */
@Controller
@RequestMapping("/maestros")
public class MaestroController {

    private final MaestroService maestroService;

    public MaestroController(MaestroService maestroService) {
        this.maestroService = maestroService;
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MAESTRO','SECRETARIA','ALUMNO')")
    public String listar(Model model) {
        model.addAttribute("maestros", maestroService.listarTodos());
        return "maestros/list";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String nuevo(Model model) {
        model.addAttribute("maestro", new Maestro());
        return "maestros/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String guardar(@ModelAttribute Maestro maestro) {
        maestroService.guardar(maestro);
        return "redirect:/maestros";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String editar(@PathVariable Long id, Model model) {
        Maestro maestro = maestroService.buscarPorId(id);
        if (maestro == null) {
            return "redirect:/maestros";
        }
        model.addAttribute("maestro", maestro);
        return "maestros/form";
    }

    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String eliminar(@PathVariable Long id) {
        maestroService.eliminar(id);
        return "redirect:/maestros";
    }
}
