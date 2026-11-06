package com.mx.development.jose.said.controller.web;

import com.mx.development.jose.said.model.Horario;
import com.mx.development.jose.said.service.AulaService;
import com.mx.development.jose.said.service.GrupoService;
import com.mx.development.jose.said.service.HorarioService;
import com.mx.development.jose.said.service.MaestroService;
import com.mx.development.jose.said.service.MateriaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador web MVC para la gestión de horarios.
 */
@Controller
@RequestMapping("/horarios")
public class HorarioController {

    private final HorarioService horarioService;
    private final MateriaService materiaService;
    private final MaestroService maestroService;
    private final AulaService aulaService;
    private final GrupoService grupoService;

    public HorarioController(HorarioService horarioService,
                             MateriaService materiaService,
                             MaestroService maestroService,
                             AulaService aulaService,
                             GrupoService grupoService) {
        this.horarioService = horarioService;
        this.materiaService = materiaService;
        this.maestroService = maestroService;
        this.aulaService = aulaService;
        this.grupoService = grupoService;
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MAESTRO','SECRETARIA','ALUMNO')")
    public String listar(Model model) {
        model.addAttribute("horarios", horarioService.listarTodos());
        return "horarios/list";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String nuevo(Model model) {
        model.addAttribute("horario", new Horario());
        cargarDatosFormulario(model);
        return "horarios/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String guardar(@ModelAttribute Horario horario) {
        horarioService.guardar(horario);
        return "redirect:/horarios";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String editar(@PathVariable Long id, Model model) {
        Horario horario = horarioService.buscarPorId(id);
        if (horario == null) {
            return "redirect:/horarios";
        }
        model.addAttribute("horario", horario);
        cargarDatosFormulario(model);
        return "horarios/form";
    }

    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','SECRETARIA')")
    public String eliminar(@PathVariable Long id) {
        horarioService.eliminar(id);
        return "redirect:/horarios";
    }

    private void cargarDatosFormulario(Model model) {
        model.addAttribute("materias", materiaService.listarTodos());
        model.addAttribute("maestros", maestroService.listarTodos());
        model.addAttribute("aulas", aulaService.listarTodos());
        model.addAttribute("grupos", grupoService.listarTodos());
    }
}
