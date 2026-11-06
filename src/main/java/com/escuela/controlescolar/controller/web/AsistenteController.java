package com.escuela.controlescolar.controller.web;

import com.escuela.controlescolar.ai.AsistenteIAService;
import com.escuela.controlescolar.ai.EscolarTools;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador web MVC para el asistente de IA.
 */
@Controller
@RequestMapping("/asistente")
public class AsistenteController {

    private final AsistenteIAService asistenteIAService;

    public AsistenteController(AsistenteIAService asistenteIAService) {
        this.asistenteIAService = asistenteIAService;
    }

    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("tools", EscolarTools.TOOL_NAMES);
        model.addAttribute("respuesta", "");
        return "asistente/index";
    }

    @PostMapping("/preguntar")
    public String preguntar(@RequestParam String pregunta, Model model) {
        model.addAttribute("respuesta", asistenteIAService.preguntar(pregunta));
        model.addAttribute("pregunta", pregunta);
        model.addAttribute("tools", EscolarTools.TOOL_NAMES);
        return "asistente/index";
    }
}
