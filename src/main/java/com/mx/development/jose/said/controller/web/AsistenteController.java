package com.mx.development.jose.said.controller.web;

import com.mx.development.jose.said.ai.AsistenteIAService;
import com.mx.development.jose.said.ai.ConocimientoEscolar;
import com.mx.development.jose.said.ai.EscolarEscrituraTools;
import com.mx.development.jose.said.ai.EscolarTools;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;

import java.util.Map;

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
    public String index(Authentication authentication, Model model) {
        prepararModelo(authentication, model);
        model.addAttribute("respuesta", "");
        return "asistente/index";
    }

    /** Respaldo sin JavaScript: responde de una sola vez y recarga la página. */
    @PostMapping("/preguntar")
    public String preguntar(@RequestParam String pregunta, Authentication authentication, Model model) {
        model.addAttribute("respuesta", asistenteIAService.preguntar(pregunta, authentication));
        model.addAttribute("pregunta", pregunta);
        prepararModelo(authentication, model);
        return "asistente/index";
    }

    /**
     * Respuesta en streaming (Server-Sent Events). Cada evento es
     * {@code {"t": "<fragmento>"}} y el último {@code {"fin": "true"}};
     * se envía como JSON para conservar los espacios iniciales de cada token.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<Map<String, String>> stream(@RequestParam String pregunta, Authentication authentication) {
        return asistenteIAService.preguntarStream(pregunta, authentication)
                .map(fragmento -> Map.of("t", fragmento))
                .concatWithValues(Map.of("fin", "true"));
    }

    @PostMapping("/limpiar")
    public String limpiar(Authentication authentication) {
        asistenteIAService.limpiarConversacion(authentication);
        return "redirect:/asistente";
    }

    private void prepararModelo(Authentication authentication, Model model) {
        model.addAttribute("tools", EscolarTools.TOOL_NAMES);
        model.addAttribute("toolsConocimiento", ConocimientoEscolar.TOOL_NAMES);
        model.addAttribute("toolsEscritura", EscolarEscrituraTools.TOOL_NAMES);
        model.addAttribute("configurado", asistenteIAService.isConfigurado());
        model.addAttribute("historial", asistenteIAService.historial(authentication));
    }
}
