package com.mx.development.jose.said.ai;

import com.mx.development.jose.said.model.Alumno;
import com.mx.development.jose.said.model.Calificacion;
import com.mx.development.jose.said.model.Materia;
import com.mx.development.jose.said.service.AlumnoService;
import com.mx.development.jose.said.service.CalificacionService;
import com.mx.development.jose.said.service.MateriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.model.ToolContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EscolarEscrituraToolsTest {

    private CalificacionService calificacionService;
    private EscolarEscrituraTools tools;

    @BeforeEach
    void setUp() {
        AlumnoService alumnoService = mock(AlumnoService.class);
        MateriaService materiaService = mock(MateriaService.class);
        calificacionService = mock(CalificacionService.class);
        tools = new EscolarEscrituraTools(alumnoService, materiaService, calificacionService);

        when(alumnoService.buscarPorMatricula("ESC-001")).thenReturn(
                new Alumno("ESC-001", "Sofía", "García", null, null, null, null, null, null, true));
        when(materiaService.buscarPorClave("MAT101")).thenReturn(
                new Materia("MAT101", "Matemáticas", null, 6, 5, null));
        when(calificacionService.guardar(any())).thenAnswer(inv -> {
            Calificacion c = inv.getArgument(0);
            c.setId(99L);
            return c;
        });
    }

    @Test
    void maestroPuedeRegistrar() {
        String resultado = tools.registrarCalificacion("ESC-001", "MAT101", 8.5, "2026-1", null,
                contexto("maestro", "ROLE_MAESTRO"));

        ArgumentCaptor<Calificacion> captor = ArgumentCaptor.forClass(Calificacion.class);
        verify(calificacionService).guardar(captor.capture());
        assertThat(captor.getValue().getValor()).isEqualTo(8.5);
        assertThat(captor.getValue().getTipoEvaluacion()).isEqualTo("PARCIAL");
        assertThat(captor.getValue().getObservaciones()).contains("maestro");
        assertThat(resultado).startsWith("Calificación registrada (id 99)");
    }

    @Test
    void alumnoNoPuedeRegistrar() {
        String resultado = tools.registrarCalificacion("ESC-001", "MAT101", 10.0, "2026-1", null,
                contexto("alumno", "ROLE_ALUMNO"));

        assertThat(resultado).startsWith("No tienes permiso");
        verify(calificacionService, never()).guardar(any());
    }

    @Test
    void sinContextoNoRegistra() {
        assertThat(tools.registrarCalificacion("ESC-001", "MAT101", 10.0, "2026-1", null, null))
                .startsWith("No tienes permiso");
        verify(calificacionService, never()).guardar(any());
    }

    @Test
    void validaRangoDeCalificacion() {
        assertThat(tools.registrarCalificacion("ESC-001", "MAT101", 11.0, "2026-1", null,
                contexto("admin", "ROLE_ADMIN"))).isEqualTo("La calificación debe estar entre 0 y 10.");
        verify(calificacionService, never()).guardar(any());
    }

    private static ToolContext contexto(String usuario, String rol) {
        return new ToolContext(Map.of(
                EscolarEscrituraTools.CTX_USUARIO, usuario,
                EscolarEscrituraTools.CTX_ROLES, List.of(rol)));
    }
}
