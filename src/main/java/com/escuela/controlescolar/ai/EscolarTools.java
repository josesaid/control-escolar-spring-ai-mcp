package com.escuela.controlescolar.ai;

import com.escuela.controlescolar.model.Alumno;
import com.escuela.controlescolar.model.Calificacion;
import com.escuela.controlescolar.model.Grupo;
import com.escuela.controlescolar.model.Horario;
import com.escuela.controlescolar.model.Maestro;
import com.escuela.controlescolar.model.Materia;
import com.escuela.controlescolar.service.AlumnoService;
import com.escuela.controlescolar.service.CalificacionService;
import com.escuela.controlescolar.service.GrupoService;
import com.escuela.controlescolar.service.HorarioService;
import com.escuela.controlescolar.service.MaestroService;
import com.escuela.controlescolar.service.MateriaService;
import com.escuela.controlescolar.service.ReporteService;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Tools del sistema de control escolar, expuestas a la vez como
 * herramientas de Spring AI ({@code @Tool}, para el ChatClient) y
 * como herramientas MCP ({@code @McpTool}, para el servidor MCP).
 * Todos los métodos devuelven texto legible y nunca {@code null}.
 */
@Component
public class EscolarTools {

    /** Nombres MCP de las 7 tools, en el mismo orden en que se declaran. */
    public static final List<String> TOOL_NAMES = List.of(
            "consultar-alumno-por-matricula",
            "promedio-alumno",
            "calificaciones-por-materia",
            "listar-horario-grupo",
            "buscar-maestro",
            "generar-reporte-grupo",
            "listar-alumnos-en-riesgo"
    );

    private final AlumnoService alumnoService;
    private final MaestroService maestroService;
    private final MateriaService materiaService;
    private final HorarioService horarioService;
    private final CalificacionService calificacionService;
    private final ReporteService reporteService;
    private final GrupoService grupoService;

    public EscolarTools(AlumnoService alumnoService,
                        MaestroService maestroService,
                        MateriaService materiaService,
                        HorarioService horarioService,
                        CalificacionService calificacionService,
                        ReporteService reporteService,
                        GrupoService grupoService) {
        this.alumnoService = alumnoService;
        this.maestroService = maestroService;
        this.materiaService = materiaService;
        this.horarioService = horarioService;
        this.calificacionService = calificacionService;
        this.reporteService = reporteService;
        this.grupoService = grupoService;
    }

    @Tool(description = "Consulta los datos de un alumno por su matrícula, incluyendo su promedio general.")
    @McpTool(name = "consultar-alumno-por-matricula",
            description = "Consulta los datos de un alumno por su matrícula, incluyendo su promedio general.")
    public String consultarAlumnoPorMatricula(
            @ToolParam(description = "Matrícula del alumno a consultar")
            @McpToolParam(description = "Matrícula del alumno a consultar") String matricula) {
        Alumno alumno = alumnoService.buscarPorMatricula(matricula);
        if (alumno == null) {
            return "No se encontró ningún alumno con la matrícula " + matricula + ".";
        }
        Double promedio = calificacionService.promedioPorAlumno(alumno.getId());
        String grupo = alumno.getGrupo() != null
                ? alumno.getGrupo().getNombre() + " (" + alumno.getGrupo().getClave() + ")"
                : "sin grupo asignado";
        return "Alumno: " + alumno.getNombreCompleto()
                + ". Matrícula: " + alumno.getMatricula()
                + ". Email: " + valorOTexto(alumno.getEmail())
                + ". Teléfono: " + valorOTexto(alumno.getTelefono())
                + ". Grupo: " + grupo
                + ". Estado: " + (alumno.isActivo() ? "activo" : "inactivo")
                + ". Promedio general: " + formatoPromedio(promedio) + ".";
    }

    @Tool(description = "Obtiene el promedio general de un alumno por su matrícula.")
    @McpTool(name = "promedio-alumno",
            description = "Obtiene el promedio general de un alumno por su matrícula.")
    public String promedioAlumno(
            @ToolParam(description = "Matrícula del alumno")
            @McpToolParam(description = "Matrícula del alumno") String matricula) {
        Alumno alumno = alumnoService.buscarPorMatricula(matricula);
        if (alumno == null) {
            return "No se encontró ningún alumno con la matrícula " + matricula + ".";
        }
        Double promedio = calificacionService.promedioPorAlumno(alumno.getId());
        if (promedio == null) {
            return "El alumno " + alumno.getNombreCompleto()
                    + " (matrícula " + matricula + ") aún no tiene calificaciones registradas.";
        }
        return "El promedio general del alumno " + alumno.getNombreCompleto()
                + " (matrícula " + matricula + ") es " + formatoPromedio(promedio) + ".";
    }

    @Tool(description = "Lista las calificaciones registradas de una materia, buscada por su clave.")
    @McpTool(name = "calificaciones-por-materia",
            description = "Lista las calificaciones registradas de una materia, buscada por su clave.")
    public String calificacionesPorMateria(
            @ToolParam(description = "Clave de la materia")
            @McpToolParam(description = "Clave de la materia") String claveMateria) {
        Materia materia = materiaService.buscarPorClave(claveMateria);
        if (materia == null) {
            return "No se encontró ninguna materia con la clave " + claveMateria + ".";
        }
        List<Calificacion> calificaciones = calificacionService.listarPorMateria(materia.getId());
        if (calificaciones == null || calificaciones.isEmpty()) {
            return "La materia " + materia.getNombre()
                    + " (" + materia.getClave() + ") no tiene calificaciones registradas.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Calificaciones de la materia ").append(materia.getNombre())
                .append(" (").append(materia.getClave()).append("):");
        for (Calificacion c : calificaciones) {
            sb.append("\n- ")
                    .append(c.getAlumno() != null ? c.getAlumno().getNombreCompleto() : "Alumno desconocido")
                    .append(": ").append(valorOTexto(c.getValor()))
                    .append(" (").append(valorOTexto(c.getTipoEvaluacion()))
                    .append(", periodo ").append(valorOTexto(c.getPeriodo())).append(")");
        }
        return sb.toString();
    }

    @Tool(description = "Lista el horario semanal de un grupo, buscado por su clave.")
    @McpTool(name = "listar-horario-grupo",
            description = "Lista el horario semanal de un grupo, buscado por su clave.")
    public String listarHorarioGrupo(
            @ToolParam(description = "Clave del grupo")
            @McpToolParam(description = "Clave del grupo") String claveGrupo) {
        Grupo grupo = grupoService.buscarPorClave(claveGrupo);
        if (grupo == null) {
            return "No se encontró ningún grupo con la clave " + claveGrupo + ".";
        }
        List<Horario> horarios = horarioService.listarPorGrupo(grupo.getId());
        if (horarios == null || horarios.isEmpty()) {
            return "El grupo " + grupo.getNombre()
                    + " (" + grupo.getClave() + ") no tiene horarios registrados.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Horario del grupo ").append(grupo.getNombre())
                .append(" (").append(grupo.getClave()).append("):");
        for (Horario h : horarios) {
            sb.append("\n- ").append(valorOTexto(h.getDiaSemana())).append(" ")
                    .append(valorOTexto(h.getHoraInicio())).append(" - ").append(valorOTexto(h.getHoraFin()))
                    .append(": ").append(h.getMateria() != null ? h.getMateria().getNombre() : "Materia desconocida")
                    .append(" con ").append(h.getMaestro() != null ? h.getMaestro().getNombreCompleto() : "maestro desconocido")
                    .append(", aula ").append(h.getAula() != null ? h.getAula().getNombre() : "sin aula");
        }
        return sb.toString();
    }

    @Tool(description = "Busca un maestro por su número de empleado y muestra sus datos y materias.")
    @McpTool(name = "buscar-maestro",
            description = "Busca un maestro por su número de empleado y muestra sus datos y materias.")
    public String buscarMaestro(
            @ToolParam(description = "Número de empleado del maestro")
            @McpToolParam(description = "Número de empleado del maestro") String numeroEmpleado) {
        Maestro maestro = maestroService.buscarPorNumeroEmpleado(numeroEmpleado);
        if (maestro == null) {
            return "No se encontró ningún maestro con el número de empleado " + numeroEmpleado + ".";
        }
        List<Materia> materias = materiaService.listarPorMaestro(maestro.getId());
        StringBuilder sb = new StringBuilder();
        sb.append("Maestro: ").append(maestro.getNombreCompleto())
                .append(". Número de empleado: ").append(maestro.getNumeroEmpleado())
                .append(". Especialidad: ").append(valorOTexto(maestro.getEspecialidad()))
                .append(". Email: ").append(valorOTexto(maestro.getEmail()))
                .append(". Teléfono: ").append(valorOTexto(maestro.getTelefono()))
                .append(". Estado: ").append(maestro.isActivo() ? "activo" : "inactivo")
                .append(".");
        if (materias == null || materias.isEmpty()) {
            sb.append(" No tiene materias asignadas.");
        } else {
            sb.append(" Materias que imparte:");
            for (Materia m : materias) {
                sb.append("\n- ").append(m.getNombre()).append(" (").append(m.getClave()).append(")");
            }
        }
        return sb.toString();
    }

    @Tool(description = "Genera un reporte de un grupo por su clave: alumnos, promedios y promedio del grupo.")
    @McpTool(name = "generar-reporte-grupo",
            description = "Genera un reporte de un grupo por su clave: alumnos, promedios y promedio del grupo.")
    public String generarReporteGrupo(
            @ToolParam(description = "Clave del grupo")
            @McpToolParam(description = "Clave del grupo") String claveGrupo) {
        Grupo grupo = grupoService.buscarPorClave(claveGrupo);
        if (grupo == null) {
            return "No se encontró ningún grupo con la clave " + claveGrupo + ".";
        }
        List<Alumno> alumnos = alumnoService.listarPorGrupo(grupo.getId());
        if (alumnos == null || alumnos.isEmpty()) {
            return "El grupo " + grupo.getNombre()
                    + " (" + grupo.getClave() + ") no tiene alumnos inscritos.";
        }
        double suma = 0.0;
        int conPromedio = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("Reporte del grupo ").append(grupo.getNombre())
                .append(" (").append(grupo.getClave())
                .append(", grado ").append(valorOTexto(grupo.getGrado()))
                .append(", turno ").append(valorOTexto(grupo.getTurno()))
                .append("). Total de alumnos: ").append(alumnos.size()).append(".");
        for (Alumno a : alumnos) {
            Double promedio = calificacionService.promedioPorAlumno(a.getId());
            sb.append("\n- ").append(a.getNombreCompleto())
                    .append(" (").append(a.getMatricula()).append("): promedio ")
                    .append(formatoPromedio(promedio));
            if (promedio != null) {
                suma += promedio;
                conPromedio++;
            }
        }
        if (conPromedio > 0) {
            sb.append("\nPromedio del grupo: ")
                    .append(String.format(Locale.ROOT, "%.2f", suma / conPromedio)).append(".");
        } else {
            sb.append("\nEl grupo aún no tiene calificaciones registradas.");
        }
        return sb.toString();
    }

    @Tool(description = "Lista los alumnos en riesgo académico (promedio bajo o reprobados).")
    @McpTool(name = "listar-alumnos-en-riesgo",
            description = "Lista los alumnos en riesgo académico (promedio bajo o reprobados).")
    public String listarAlumnosEnRiesgo() {
        List<Map<String, Object>> enRiesgo = reporteService.alumnosEnRiesgo();
        if (enRiesgo == null || enRiesgo.isEmpty()) {
            return "No hay alumnos en riesgo en este momento.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Alumnos en riesgo académico (").append(enRiesgo.size()).append("):");
        for (Map<String, Object> alumno : enRiesgo) {
            sb.append("\n- ");
            boolean primero = true;
            for (Map.Entry<String, Object> entry : alumno.entrySet()) {
                if (!primero) {
                    sb.append("; ");
                }
                sb.append(entry.getKey()).append(": ").append(valorOTexto(entry.getValue()));
                primero = false;
            }
        }
        return sb.toString();
    }

    @McpResource(uri = "escolar://materias",
            name = "listar-materias",
            description = "Lista todas las materias en JSON",
            mimeType = "application/json")
    public String listarMateriasResource() {
        List<Materia> materias = materiaService.listarTodos();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < materias.size(); i++) {
            Materia m = materias.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{\"clave\":\"").append(escapeJson(m.getClave()))
                    .append("\",\"nombre\":\"").append(escapeJson(m.getNombre())).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    @McpPrompt(name = "resumen-escolar",
            description = "Genera un resumen escolar")
    public String resumenEscolarPrompt() {
        return "Eres un asistente escolar del sistema de control escolar. "
                + "Genera un resumen escolar claro y estructurado, en español, con los siguientes apartados: "
                + "1) Totales: número de alumnos, maestros, materias y grupos. "
                + "2) Promedio general de calificaciones. "
                + "3) Top 5 de alumnos con mejor promedio. "
                + "4) Alumnos en riesgo académico y materias con más reprobados. "
                + "Usa las tools disponibles para consultar los datos reales del sistema antes de responder "
                + "y no inventes cifras: si un dato no está disponible, indícalo claramente. "
                + "Termina con una recomendación breve para la dirección de la escuela.";
    }

    private String formatoPromedio(Double promedio) {
        if (promedio == null) {
            return "sin calificaciones";
        }
        return String.format(Locale.ROOT, "%.2f", promedio);
    }

    private String valorOTexto(Object valor) {
        return valor == null ? "no registrado" : String.valueOf(valor);
    }

    private String escapeJson(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
