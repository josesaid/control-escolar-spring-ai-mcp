package com.escuela.controlescolar.ai;

import com.escuela.controlescolar.model.Alumno;
import com.escuela.controlescolar.model.Calificacion;
import com.escuela.controlescolar.model.Materia;
import com.escuela.controlescolar.service.AlumnoService;
import com.escuela.controlescolar.service.CalificacionService;
import com.escuela.controlescolar.service.MateriaService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Tools de escritura, disponibles solo para el ChatClient ({@code @Tool}).
 *
 * No se exponen como {@code @McpTool} a propósito: el endpoint {@code /mcp}
 * no tiene usuario autenticado, así que nadie podría validar permisos.
 * El usuario y sus roles llegan en el {@link ToolContext} que
 * {@link AsistenteIAService} arma en cada petición (claves
 * {@link #CTX_USUARIO} y {@link #CTX_ROLES}); el modelo nunca los ve.
 */
@Component
public class EscolarEscrituraTools {

    public static final String CTX_USUARIO = "usuario";
    public static final String CTX_ROLES = "roles";

    /** Nombres de las tools de escritura, para mostrarlas en la vista. */
    public static final List<String> TOOL_NAMES = List.of("registrarCalificacion");

    private static final Set<String> ROLES_PERMITIDOS = Set.of("ROLE_ADMIN", "ROLE_DIRECTOR", "ROLE_MAESTRO");

    private final AlumnoService alumnoService;
    private final MateriaService materiaService;
    private final CalificacionService calificacionService;

    public EscolarEscrituraTools(AlumnoService alumnoService,
                                 MateriaService materiaService,
                                 CalificacionService calificacionService) {
        this.alumnoService = alumnoService;
        this.materiaService = materiaService;
        this.calificacionService = calificacionService;
    }

    @Tool(description = "Registra una calificación (0 a 10) de un alumno en una materia. "
            + "Solo pueden usarla administradores, directores y maestros. "
            + "Confirma con el usuario los datos antes de llamarla.")
    public String registrarCalificacion(
            @ToolParam(description = "Matrícula del alumno") String matricula,
            @ToolParam(description = "Clave de la materia") String claveMateria,
            @ToolParam(description = "Calificación de 0 a 10") Double valor,
            @ToolParam(description = "Periodo escolar, por ejemplo 2026-1") String periodo,
            @ToolParam(description = "Tipo de evaluación, por ejemplo PARCIAL, EXAMEN o PROYECTO",
                    required = false) String tipoEvaluacion,
            ToolContext toolContext) {
        if (!tienePermiso(toolContext)) {
            return "No tienes permiso para registrar calificaciones. "
                    + "Solo administradores, directores y maestros pueden hacerlo.";
        }
        if (valor == null || valor < 0 || valor > 10) {
            return "La calificación debe estar entre 0 y 10.";
        }
        if (periodo == null || periodo.isBlank()) {
            return "Indica el periodo escolar de la calificación, por ejemplo 2026-1.";
        }
        Alumno alumno = alumnoService.buscarPorMatricula(matricula);
        if (alumno == null) {
            return "No se encontró ningún alumno con la matrícula " + matricula + ".";
        }
        Materia materia = materiaService.buscarPorClave(claveMateria);
        if (materia == null) {
            return "No se encontró ninguna materia con la clave " + claveMateria + ".";
        }
        String tipo = tipoEvaluacion == null || tipoEvaluacion.isBlank() ? "PARCIAL" : tipoEvaluacion;
        Calificacion guardada = calificacionService.guardar(new Calificacion(alumno, materia, periodo.trim(),
                valor, tipo, LocalDate.now(), "Registrada por el asistente IA (usuario " + usuario(toolContext) + ")"));
        return "Calificación registrada (id " + guardada.getId() + "): " + alumno.getNombreCompleto()
                + ", " + materia.getNombre() + ", " + valor + " (" + tipo + ", periodo " + periodo.trim() + ").";
    }

    private boolean tienePermiso(ToolContext toolContext) {
        if (toolContext == null || toolContext.getContext() == null) {
            return false;
        }
        Object roles = toolContext.getContext().get(CTX_ROLES);
        if (!(roles instanceof Collection<?> lista)) {
            return false;
        }
        return lista.stream().anyMatch(r -> ROLES_PERMITIDOS.contains(String.valueOf(r)));
    }

    private String usuario(ToolContext toolContext) {
        Object usuario = toolContext.getContext().get(CTX_USUARIO);
        return usuario == null ? "desconocido" : String.valueOf(usuario);
    }
}
