package com.mx.development.jose.said.controller.web;

import com.mx.development.jose.said.service.ReporteService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador web MVC para los reportes y la exportación a CSV.
 */
@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("resumen", reporteService.resumenGeneral());
        model.addAttribute("promedioPorGrupo", reporteService.promedioPorGrupo());
        model.addAttribute("promedioPorMateria", reporteService.promedioPorMateria());
        model.addAttribute("topAlumnos", reporteService.topAlumnos(10));
        model.addAttribute("alumnosEnRiesgo", reporteService.alumnosEnRiesgo());
        model.addAttribute("reprobadosPorMateria", reporteService.reprobadosPorMateria());
        return "reportes/index";
    }

    @GetMapping("/exportar-csv")
    public ResponseEntity<String> exportarCsv() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("reporte-calificaciones.csv").build());
        return ResponseEntity.ok()
                .headers(headers)
                .body(reporteService.generarCsv());
    }
}
