package com.escuela.controlescolar.ai;

import com.escuela.controlescolar.model.Alumno;
import com.escuela.controlescolar.model.Asistencia;
import com.escuela.controlescolar.model.Calificacion;
import com.escuela.controlescolar.model.Grupo;
import com.escuela.controlescolar.model.Materia;
import com.escuela.controlescolar.service.AlumnoService;
import com.escuela.controlescolar.service.AsistenciaService;
import com.escuela.controlescolar.service.AulaService;
import com.escuela.controlescolar.service.CalificacionService;
import com.escuela.controlescolar.service.GrupoService;
import com.escuela.controlescolar.service.HorarioService;
import com.escuela.controlescolar.service.MaestroService;
import com.escuela.controlescolar.service.MateriaService;
import com.escuela.controlescolar.service.ReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EscolarToolsTest {

    private AlumnoService alumnoService;
    private MateriaService materiaService;
    private CalificacionService calificacionService;
    private ReporteService reporteService;
    private GrupoService grupoService;
    private AsistenciaService asistenciaService;
    private EscolarTools tools;

    private Alumno sofia;

    @BeforeEach
    void setUp() {
        alumnoService = mock(AlumnoService.class);
        materiaService = mock(MateriaService.class);
        calificacionService = mock(CalificacionService.class);
        reporteService = mock(ReporteService.class);
        grupoService = mock(GrupoService.class);
        asistenciaService = mock(AsistenciaService.class);
        tools = new EscolarTools(alumnoService, mock(MaestroService.class), materiaService,
                mock(HorarioService.class), calificacionService, reporteService, grupoService,
                asistenciaService, mock(AulaService.class));

        Grupo grupo = new Grupo("1A", "Primero A", "1", "MATUTINO", 30);
        sofia = new Alumno("ESC-001", "Sofía", "García", "Mendoza", null, null, null, null, grupo, true);
        sofia.setId(1L);
        when(alumnoService.buscarPorMatricula("ESC-001")).thenReturn(sofia);
    }

    @Test
    void consultarAlumnoIncluyePromedioYGrupo() {
        when(calificacionService.promedioPorAlumno(1L)).thenReturn(8.456);

        String resultado = tools.consultarAlumnoPorMatricula("ESC-001");

        assertThat(resultado).contains("ESC-001", "Primero A (1A)", "Promedio general: 8.46");
    }

    @Test
    void alumnoInexistenteDevuelveMensajeClaro() {
        assertThat(tools.promedioAlumno("NO-EXISTE"))
                .isEqualTo("No se encontró ningún alumno con la matrícula NO-EXISTE.");
        assertThat(tools.porcentajeAsistenciaAlumno("NO-EXISTE")).contains("No se encontró");
    }

    @Test
    void porcentajeAsistenciaCuentaFaltas() {
        when(asistenciaService.listarPorAlumno(1L)).thenReturn(List.of(
                new Asistencia(sofia, null, LocalDate.now(), true, null),
                new Asistencia(sofia, null, LocalDate.now(), true, null),
                new Asistencia(sofia, null, LocalDate.now(), true, null),
                new Asistencia(sofia, null, LocalDate.now(), false, null)));
        when(asistenciaService.porcentajeAsistenciaPorAlumno(1L)).thenReturn(75.0);

        assertThat(tools.porcentajeAsistenciaAlumno("ESC-001"))
                .contains("75.00% de asistencia", "4 registros, 1 faltas");
    }

    @Test
    void boletaDelPeriodoCalculaPromedio() {
        Materia mate = new Materia("MAT101", "Matemáticas", null, 6, 5, null);
        when(calificacionService.listarPorAlumnoYPeriodo(1L, "2026-1")).thenReturn(List.of(
                new Calificacion(sofia, mate, "2026-1", 9.0, "PARCIAL", null, null),
                new Calificacion(sofia, mate, "2026-1", 7.0, "EXAMEN", null, null)));

        assertThat(tools.calificacionesAlumnoPeriodo("ESC-001", "2026-1"))
                .contains("Matemáticas: 9.0 (PARCIAL)", "Promedio del periodo: 8.00");
    }

    @Test
    void promedioPorGrupoOrdenaDeMayorAMenor() {
        when(reporteService.promedioPorGrupo()).thenReturn(List.of(
                fila("grupo", "Primero A", "clave", "1A", "totalAlumnos", 3, "promedio", 7.0),
                fila("grupo", "Segundo A", "clave", "2A", "totalAlumnos", 2, "promedio", 9.0)));

        String resultado = tools.promedioPorGrupo();

        assertThat(resultado.indexOf("Segundo A")).isLessThan(resultado.indexOf("Primero A"));
    }

    @Test
    void topAlumnosUsaCincoPorDefectoYLimitaA50() {
        when(reporteService.topAlumnos(5)).thenReturn(List.of(
                fila("nombre", "Sofía García", "matricula", "ESC-001", "promedio", 9.5)));

        assertThat(tools.topAlumnos(null)).contains("1. Sofía García (ESC-001): 9.50");
        tools.topAlumnos(500);
        verify(reporteService).topAlumnos(50);
    }

    @Test
    void completionsFiltranPorPrefijoSinImportarMayusculas() {
        Alumno otro = new Alumno("ESC-002", "Diego", "Hernández", null, null, null, null, null, null, true);
        Alumno externo = new Alumno("EXT-001", "Ana", "Pérez", null, null, null, null, null, null, true);
        when(alumnoService.listarTodos()).thenReturn(List.of(otro, externo, sofia));

        assertThat(tools.completarMatriculaPrompt("esc")).containsExactly("ESC-001", "ESC-002");
        assertThat(tools.completarMatriculaPrompt("")).hasSize(3);
    }

    @Test
    void fichaAlumnoResourceEsJson() {
        when(calificacionService.promedioPorAlumno(1L)).thenReturn(8.0);
        when(asistenciaService.listarPorAlumno(1L)).thenReturn(List.of(
                new Asistencia(sofia, null, LocalDate.now(), true, null)));
        when(asistenciaService.porcentajeAsistenciaPorAlumno(1L)).thenReturn(95.0);

        assertThat(tools.fichaAlumnoResource("ESC-001")).isEqualTo(
                "{\"matricula\":\"ESC-001\",\"nombre\":\"" + sofia.getNombreCompleto()
                        + "\",\"grupo\":\"1A\",\"activo\":true,\"promedio\":8.00,\"porcentajeAsistencia\":95.00}");
    }

    @Test
    void fichaAlumnoSinAsistenciasDevuelveNull() {
        assertThat(tools.fichaAlumnoResource("ESC-001")).contains("\"porcentajeAsistencia\":null");
    }

    @Test
    void listaDeNombresCoincideConLasTools() {
        assertThat(EscolarTools.TOOL_NAMES).hasSize(15).doesNotHaveDuplicates();
    }

    private static Map<String, Object> fila(Object... claveValor) {
        Map<String, Object> fila = new LinkedHashMap<>();
        for (int i = 0; i < claveValor.length; i += 2) {
            fila.put((String) claveValor[i], claveValor[i + 1]);
        }
        return fila;
    }
}
