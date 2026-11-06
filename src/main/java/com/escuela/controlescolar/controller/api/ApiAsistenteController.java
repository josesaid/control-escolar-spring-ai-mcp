package com.escuela.controlescolar.controller.api;

import com.escuela.controlescolar.ai.AnalisisAlumno;
import com.escuela.controlescolar.ai.AsistenteIAService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * API REST del asistente de IA.
 */
@RestController
@RequestMapping("/api/ia")
public class ApiAsistenteController {

    private final AsistenteIAService asistenteIAService;

    public ApiAsistenteController(AsistenteIAService asistenteIAService) {
        this.asistenteIAService = asistenteIAService;
    }

    /** Pregunta libre al asistente; usa la memoria de la conversación del usuario autenticado. */
    @PostMapping("/preguntar")
    public Map<String, String> preguntar(@RequestBody Map<String, String> cuerpo, Authentication authentication) {
        String pregunta = cuerpo.getOrDefault("pregunta", "");
        return Map.of("pregunta", pregunta,
                "respuesta", asistenteIAService.preguntar(pregunta, authentication));
    }

    /** Análisis académico de un alumno como JSON tipado (structured output). */
    @GetMapping("/analisis-alumno/{matricula}")
    public ResponseEntity<?> analisisAlumno(@PathVariable String matricula) {
        if (!asistenteIAService.isConfigurado()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "El asistente IA no está configurado: define OPENAI_API_KEY."));
        }
        try {
            AnalisisAlumno analisis = asistenteIAService.analizarAlumno(matricula);
            return ResponseEntity.ok(analisis);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "No pude consultar el modelo de IA: " + e.getMessage()));
        }
    }
}
