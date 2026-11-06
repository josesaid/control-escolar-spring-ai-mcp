package com.mx.development.jose.said.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/** Sin clave de OpenAI: no se indexa nada y se usa la búsqueda por palabras clave. */
class ConocimientoEscolarTest {

    private ObjectProvider<EmbeddingModel> provider;
    private ConocimientoEscolar conocimiento;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        provider = mock(ObjectProvider.class);
        conocimiento = new ConocimientoEscolar(provider,
                new MockEnvironment().withProperty("spring.ai.openai.api-key", "demo"));
        conocimiento.init();
    }

    @Test
    void sinClaveNoCreaVectorStore() {
        assertThat(conocimiento.getVectorStore()).isNull();
        verifyNoInteractions(provider);
    }

    @Test
    void encuentraLaCalificacionMinimaSinAcentos() {
        assertThat(conocimiento.consultarReglamento("calificacion minima aprobatoria"))
                .startsWith("[reglamento-escolar - Escala de calificaciones]")
                .contains("6.0");
    }

    @Test
    void encuentraFechasDelCalendario() {
        assertThat(conocimiento.consultarReglamento("¿Cuándo son las reinscripciones?"))
                .contains("del 3 al 14 de agosto");
    }

    @Test
    void temaInexistente() {
        assertThat(conocimiento.consultarReglamento("xyzw")).startsWith("No encontré información");
    }

    @Test
    void recursosCompletos() {
        assertThat(conocimiento.reglamentoResource()).startsWith("# Reglamento escolar");
        assertThat(conocimiento.calendarioResource()).startsWith("# Calendario escolar 2026");
    }
}
