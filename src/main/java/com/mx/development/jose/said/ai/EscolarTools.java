package com.mx.development.jose.said.ai;

import com.mx.development.jose.said.model.Alumno;
import com.mx.development.jose.said.model.Asistencia;
import com.mx.development.jose.said.model.Aula;
import com.mx.development.jose.said.model.Calificacion;
import com.mx.development.jose.said.model.Grupo;
import com.mx.development.jose.said.model.Horario;
import com.mx.development.jose.said.model.Maestro;
import com.mx.development.jose.said.model.Materia;
import com.mx.development.jose.said.service.AlumnoService;
import com.mx.development.jose.said.service.AsistenciaService;
import com.mx.development.jose.said.service.AulaService;
import com.mx.development.jose.said.service.CalificacionService;
import com.mx.development.jose.said.service.GrupoService;
import com.mx.development.jose.said.service.HorarioService;
import com.mx.development.jose.said.service.MaestroService;
import com.mx.development.jose.said.service.MateriaService;
import com.mx.development.jose.said.service.ReporteService;
import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpComplete;
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
import java.util.function.Function;

/**
 * Tools del sistema de control escolar, expuestas a la vez como
 * herramientas de Spring AI ({@code @Tool}, para el ChatClient) y
 * como herramientas MCP ({@code @McpTool}, para el servidor MCP).
 * Todos los métodos devuelven texto legible y nunca {@code null}.
 */
@Component
public class EscolarTools {

    /** Nombres MCP de las 15 tools, en el mismo orden en que se declaran. */
    public static final List<String> TOOL_NAMES = List.of(
            "consultar-alumno-por-matricula",
            "promedio-alumno",
            "calificaciones-por-materia",
            "listar-horario-grupo",
            "buscar-maestro",
            "generar-reporte-grupo",
            "listar-alumnos-en-riesgo",
            "porcentaje-asistencia-alumno",
            "calificaciones-alumno-periodo",
            "horario-maestro",
            "listar-aulas",
            "promedio-por-materia",
            "promedio-por-grupo",
            "resumen-general",
            "top-alumnos"
    );

    /** Máximo de sugerencias que devuelven las completions MCP. */
    private static final int MAX_SUGERENCIAS = 10;

    /** Límite por defecto de la tool top-alumnos. */
    private static final int TOP_POR_DEFECTO = 5;

    private final AlumnoService alumnoService;
    private final MaestroService maestroService;
    private final MateriaService materiaService;
    private final HorarioService horarioService;
    private final CalificacionService calificacionService;
    private final ReporteService reporteService;
    private final GrupoService grupoService;
    private final AsistenciaService asistenciaService;
    private final AulaService aulaService;

    public EscolarTools(AlumnoService alumnoService,
                        MaestroService maestroService,
                        MateriaService materiaService,
                        HorarioService horarioService,
                        CalificacionService calificacionService,
                        ReporteService reporteService,
                        GrupoService grupoService,
                        AsistenciaService asistenciaService,
                        AulaService aulaService) {
        this.alumnoService = alumnoService;
        this.maestroService = maestroService;
        this.materiaService = materiaService;
        this.horarioService = horarioService;
        this.calificacionService = calificacionService;
        this.reporteService = reporteService;
        this.grupoService = grupoService;
        this.asistenciaService = asistenciaService;
        this.aulaService = aulaService;
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

    @Tool(description = "Obtiene el porcentaje de asistencia de un alumno por su matrícula.")
    @McpTool(name = "porcentaje-asistencia-alumno",
            description = "Obtiene el porcentaje de asistencia de un alumno por su matrícula.")
    public String porcentajeAsistenciaAlumno(
            @ToolParam(description = "Matrícula del alumno")
            @McpToolParam(description = "Matrícula del alumno") String matricula) {
        Alumno alumno = alumnoService.buscarPorMatricula(matricula);
        if (alumno == null) {
            return "No se encontró ningún alumno con la matrícula " + matricula + ".";
        }
        List<Asistencia> registros = asistenciaService.listarPorAlumno(alumno.getId());
        if (registros == null || registros.isEmpty()) {
            return "El alumno " + alumno.getNombreCompleto()
                    + " (matrícula " + matricula + ") no tiene asistencias registradas.";
        }
        long faltas = registros.stream()
                .filter(a -> a != null && !Boolean.TRUE.equals(a.getPresente()))
                .count();
        Double porcentaje = asistenciaService.porcentajeAsistenciaPorAlumno(alumno.getId());
        return "El alumno " + alumno.getNombreCompleto()
                + " (matrícula " + matricula + ") tiene "
                + String.format(Locale.ROOT, "%.2f", porcentaje) + "% de asistencia"
                + " (" + registros.size() + " registros, " + faltas + " faltas).";
    }

    @Tool(description = "Lista la boleta de un alumno en un periodo: calificaciones por materia y promedio del periodo.")
    @McpTool(name = "calificaciones-alumno-periodo",
            description = "Lista la boleta de un alumno en un periodo: calificaciones por materia y promedio del periodo.")
    public String calificacionesAlumnoPeriodo(
            @ToolParam(description = "Matrícula del alumno")
            @McpToolParam(description = "Matrícula del alumno") String matricula,
            @ToolParam(description = "Periodo escolar, por ejemplo 2026-1")
            @McpToolParam(description = "Periodo escolar, por ejemplo 2026-1") String periodo) {
        Alumno alumno = alumnoService.buscarPorMatricula(matricula);
        if (alumno == null) {
            return "No se encontró ningún alumno con la matrícula " + matricula + ".";
        }
        List<Calificacion> calificaciones =
                calificacionService.listarPorAlumnoYPeriodo(alumno.getId(), periodo);
        if (calificaciones == null || calificaciones.isEmpty()) {
            return "El alumno " + alumno.getNombreCompleto()
                    + " no tiene calificaciones registradas en el periodo " + periodo + ".";
        }
        double suma = 0.0;
        int total = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("Boleta de ").append(alumno.getNombreCompleto())
                .append(" (").append(alumno.getMatricula()).append("), periodo ").append(periodo).append(":");
        for (Calificacion c : calificaciones) {
            sb.append("\n- ")
                    .append(c.getMateria() != null ? c.getMateria().getNombre() : "Materia desconocida")
                    .append(": ").append(valorOTexto(c.getValor()))
                    .append(" (").append(valorOTexto(c.getTipoEvaluacion())).append(")");
            if (c.getValor() != null) {
                suma += c.getValor();
                total++;
            }
        }
        if (total > 0) {
            sb.append("\nPromedio del periodo: ")
                    .append(String.format(Locale.ROOT, "%.2f", suma / total)).append(".");
        }
        return sb.toString();
    }

    @Tool(description = "Lista el horario semanal de un maestro, buscado por su número de empleado.")
    @McpTool(name = "horario-maestro",
            description = "Lista el horario semanal de un maestro, buscado por su número de empleado.")
    public String horarioMaestro(
            @ToolParam(description = "Número de empleado del maestro")
            @McpToolParam(description = "Número de empleado del maestro") String numeroEmpleado) {
        Maestro maestro = maestroService.buscarPorNumeroEmpleado(numeroEmpleado);
        if (maestro == null) {
            return "No se encontró ningún maestro con el número de empleado " + numeroEmpleado + ".";
        }
        List<Horario> horarios = horarioService.listarPorMaestro(maestro.getId());
        if (horarios == null || horarios.isEmpty()) {
            return "El maestro " + maestro.getNombreCompleto() + " no tiene horarios asignados.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Horario del maestro ").append(maestro.getNombreCompleto())
                .append(" (").append(maestro.getNumeroEmpleado()).append("):");
        for (Horario h : horarios) {
            sb.append("\n- ").append(valorOTexto(h.getDiaSemana())).append(" ")
                    .append(valorOTexto(h.getHoraInicio())).append(" - ").append(valorOTexto(h.getHoraFin()))
                    .append(": ").append(h.getMateria() != null ? h.getMateria().getNombre() : "Materia desconocida")
                    .append(", grupo ").append(h.getGrupo() != null ? h.getGrupo().getClave() : "sin grupo")
                    .append(", aula ").append(h.getAula() != null ? h.getAula().getNombre() : "sin aula");
        }
        return sb.toString();
    }

    @Tool(description = "Lista las aulas de la escuela con su capacidad, edificio y tipo.")
    @McpTool(name = "listar-aulas",
            description = "Lista las aulas de la escuela con su capacidad, edificio y tipo.")
    public String listarAulas() {
        List<Aula> aulas = aulaService.listarTodos();
        if (aulas == null || aulas.isEmpty()) {
            return "No hay aulas registradas.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Aulas registradas (").append(aulas.size()).append("):");
        for (Aula a : aulas) {
            sb.append("\n- ").append(valorOTexto(a.getNombre()))
                    .append(" (").append(valorOTexto(a.getClave())).append("): capacidad ")
                    .append(valorOTexto(a.getCapacidad()))
                    .append(", edificio ").append(valorOTexto(a.getEdificio()))
                    .append(", tipo ").append(valorOTexto(a.getTipo()));
        }
        return sb.toString();
    }

    @Tool(description = "Ranking de materias ordenadas de mayor a menor promedio, con su número de calificaciones.")
    @McpTool(name = "promedio-por-materia",
            description = "Ranking de materias ordenadas de mayor a menor promedio, con su número de calificaciones.")
    public String promedioPorMateria() {
        List<Map<String, Object>> filas = ordenarPorPromedio(reporteService.promedioPorMateria());
        if (filas.isEmpty()) {
            return "No hay materias registradas.";
        }
        StringBuilder sb = new StringBuilder("Promedio por materia (de mayor a menor):");
        for (Map<String, Object> fila : filas) {
            sb.append("\n- ").append(fila.get("materia"))
                    .append(" (").append(fila.get("clave")).append("): ")
                    .append(formatoPromedio(((Number) fila.get("promedio")).doubleValue()))
                    .append(" con ").append(fila.get("totalCalificaciones")).append(" calificaciones");
        }
        return sb.toString();
    }

    @Tool(description = "Comparativo de grupos ordenados de mayor a menor promedio, con su número de alumnos.")
    @McpTool(name = "promedio-por-grupo",
            description = "Comparativo de grupos ordenados de mayor a menor promedio, con su número de alumnos.")
    public String promedioPorGrupo() {
        List<Map<String, Object>> filas = ordenarPorPromedio(reporteService.promedioPorGrupo());
        if (filas.isEmpty()) {
            return "No hay grupos registrados.";
        }
        StringBuilder sb = new StringBuilder("Promedio por grupo (de mayor a menor):");
        for (Map<String, Object> fila : filas) {
            sb.append("\n- ").append(fila.get("grupo"))
                    .append(" (").append(fila.get("clave")).append("): ")
                    .append(formatoPromedio(((Number) fila.get("promedio")).doubleValue()))
                    .append(" con ").append(fila.get("totalAlumnos")).append(" alumnos");
        }
        return sb.toString();
    }

    @Tool(description = "Resumen general de la escuela: totales de alumnos, maestros, materias, grupos y promedio general.")
    @McpTool(name = "resumen-general",
            description = "Resumen general de la escuela: totales de alumnos, maestros, materias, grupos y promedio general.")
    public String resumenGeneral() {
        Map<String, Object> resumen = reporteService.resumenGeneral();
        Object promedio = resumen.get("promedioGeneral");
        return "Resumen general de la escuela: "
                + resumen.get("totalAlumnos") + " alumnos, "
                + resumen.get("totalMaestros") + " maestros, "
                + resumen.get("totalMaterias") + " materias, "
                + resumen.get("totalGrupos") + " grupos y "
                + resumen.get("totalCalificaciones") + " calificaciones registradas. "
                + "Promedio general: "
                + formatoPromedio(promedio instanceof Number n ? n.doubleValue() : null) + ".";
    }

    @Tool(description = "Lista los alumnos con mejor promedio general (por defecto los 5 primeros).")
    @McpTool(name = "top-alumnos",
            description = "Lista los alumnos con mejor promedio general (por defecto los 5 primeros).")
    public String topAlumnos(
            @ToolParam(description = "Número de alumnos a listar (1 a 50)", required = false)
            @McpToolParam(description = "Número de alumnos a listar (1 a 50)", required = false) Integer limite) {
        int n = limite == null || limite <= 0 ? TOP_POR_DEFECTO : Math.min(limite, 50);
        List<Map<String, Object>> top = reporteService.topAlumnos(n);
        if (top == null || top.isEmpty()) {
            return "No hay alumnos registrados.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Top ").append(top.size()).append(" alumnos por promedio:");
        int posicion = 1;
        for (Map<String, Object> fila : top) {
            sb.append("\n").append(posicion++).append(". ").append(fila.get("nombre"))
                    .append(" (").append(fila.get("matricula")).append("): ")
                    .append(formatoPromedio(((Number) fila.get("promedio")).doubleValue()));
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

    @McpResource(uri = "escolar://grupos",
            name = "listar-grupos",
            description = "Lista todos los grupos en JSON",
            mimeType = "application/json")
    public String listarGruposResource() {
        List<Grupo> grupos = grupoService.listarTodos();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < grupos.size(); i++) {
            Grupo g = grupos.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{\"clave\":\"").append(escapeJson(g.getClave()))
                    .append("\",\"nombre\":\"").append(escapeJson(g.getNombre()))
                    .append("\",\"grado\":\"").append(escapeJson(g.getGrado()))
                    .append("\",\"turno\":\"").append(escapeJson(g.getTurno())).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    @McpResource(uri = "escolar://maestros",
            name = "listar-maestros",
            description = "Lista todos los maestros en JSON",
            mimeType = "application/json")
    public String listarMaestrosResource() {
        List<Maestro> maestros = maestroService.listarTodos();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < maestros.size(); i++) {
            Maestro m = maestros.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{\"numeroEmpleado\":\"").append(escapeJson(m.getNumeroEmpleado()))
                    .append("\",\"nombre\":\"").append(escapeJson(m.getNombreCompleto()))
                    .append("\",\"especialidad\":\"").append(escapeJson(m.getEspecialidad()))
                    .append("\",\"activo\":").append(m.isActivo()).append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    @McpResource(uri = "escolar://alumnos/{matricula}",
            name = "ficha-alumno",
            description = "Ficha de un alumno en JSON: datos, grupo, promedio y porcentaje de asistencia",
            mimeType = "application/json")
    public String fichaAlumnoResource(String matricula) {
        Alumno alumno = alumnoService.buscarPorMatricula(matricula);
        if (alumno == null) {
            return "{\"error\":\"No se encontró el alumno " + escapeJson(matricula) + "\"}";
        }
        Double promedio = calificacionService.promedioPorAlumno(alumno.getId());
        boolean sinAsistencias = asistenciaService.listarPorAlumno(alumno.getId()).isEmpty();
        Double asistencia = asistenciaService.porcentajeAsistenciaPorAlumno(alumno.getId());
        return "{\"matricula\":\"" + escapeJson(alumno.getMatricula())
                + "\",\"nombre\":\"" + escapeJson(alumno.getNombreCompleto())
                + "\",\"grupo\":\"" + escapeJson(alumno.getGrupo() != null ? alumno.getGrupo().getClave() : "")
                + "\",\"activo\":" + alumno.isActivo()
                + ",\"promedio\":" + (promedio == null ? "null" : String.format(Locale.ROOT, "%.2f", promedio))
                + ",\"porcentajeAsistencia\":"
                + (sinAsistencias ? "null" : String.format(Locale.ROOT, "%.2f", asistencia))
                + "}";
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

    @McpPrompt(name = "analisis-alumno",
            description = "Genera un análisis académico de un alumno")
    public String analisisAlumnoPrompt(
            @McpArg(name = "matricula", description = "Matrícula del alumno", required = true) String matricula) {
        return "Eres un orientador escolar. Analiza el desempeño del alumno con matrícula " + matricula + ". "
                + "Usa las tools consultar-alumno-por-matricula, calificaciones-alumno-periodo "
                + "y porcentaje-asistencia-alumno para obtener datos reales. "
                + "Responde en español con: 1) Datos generales. 2) Fortalezas (materias con mejor calificación). "
                + "3) Áreas de oportunidad. 4) Nivel de riesgo (bajo, medio o alto) justificado con su promedio "
                + "y su asistencia. 5) Tres recomendaciones concretas para el alumno y sus padres. "
                + "No inventes datos: si algo no está registrado, dilo.";
    }

    @McpPrompt(name = "plan-mejora-grupo",
            description = "Genera un plan de mejora académica para un grupo")
    public String planMejoraGrupoPrompt(
            @McpArg(name = "claveGrupo", description = "Clave del grupo", required = true) String claveGrupo) {
        return "Eres un coordinador académico. Elabora un plan de mejora para el grupo " + claveGrupo + ". "
                + "Usa las tools generar-reporte-grupo, listar-horario-grupo, promedio-por-materia "
                + "y listar-alumnos-en-riesgo para obtener datos reales. "
                + "Responde en español con: 1) Diagnóstico del grupo (promedio y comparación con otros grupos). "
                + "2) Alumnos que requieren atención. 3) Materias críticas. "
                + "4) Plan de acción a 4 semanas con responsables (maestros) y metas medibles. "
                + "No inventes datos: si algo no está registrado, dilo.";
    }

    @McpComplete(prompt = "analisis-alumno")
    public List<String> completarMatriculaPrompt(String valor) {
        return sugerencias(alumnoService.listarTodos(), Alumno::getMatricula, valor);
    }

    @McpComplete(uri = "escolar://alumnos/{matricula}")
    public List<String> completarMatriculaResource(String valor) {
        return sugerencias(alumnoService.listarTodos(), Alumno::getMatricula, valor);
    }

    @McpComplete(prompt = "plan-mejora-grupo")
    public List<String> completarClaveGrupo(String valor) {
        return sugerencias(grupoService.listarTodos(), Grupo::getClave, valor);
    }

    private <T> List<String> sugerencias(List<T> elementos, Function<T, String> clave, String prefijo) {
        String filtro = prefijo == null ? "" : prefijo.trim().toUpperCase(Locale.ROOT);
        return elementos.stream()
                .map(clave)
                .filter(c -> c != null && c.toUpperCase(Locale.ROOT).startsWith(filtro))
                .sorted()
                .limit(MAX_SUGERENCIAS)
                .toList();
    }

    private List<Map<String, Object>> ordenarPorPromedio(List<Map<String, Object>> filas) {
        if (filas == null) {
            return List.of();
        }
        return filas.stream()
                .sorted((a, b) -> Double.compare(((Number) b.get("promedio")).doubleValue(),
                        ((Number) a.get("promedio")).doubleValue()))
                .toList();
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
