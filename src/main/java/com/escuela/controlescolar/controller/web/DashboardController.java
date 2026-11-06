package com.escuela.controlescolar.controller.web;

import com.escuela.controlescolar.service.ReporteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Página principal, tablero (dashboard) y acceso (login).
 */
@Controller
public class DashboardController {

    private final ReporteService reporteService;

    public DashboardController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("resumen", reporteService.resumenGeneral());
        model.addAttribute("topAlumnos", reporteService.topAlumnos(5));
        model.addAttribute("alumnosEnRiesgo", reporteService.alumnosEnRiesgo());
        model.addAttribute("promedioPorMateria", reporteService.promedioPorMateria());
        return "dashboard/index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
