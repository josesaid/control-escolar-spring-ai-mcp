package com.escuela.controlescolar.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Evaluación del asistente contra el modelo real con {@link RelevancyEvaluator}
 * (un segundo modelo juzga si la respuesta es relevante para la pregunta y el contexto).
 * Solo se ejecuta si {@code OPENAI_API_KEY} tiene una clave real; tiene costo.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = "sk-.+")
class AsistenteIAEvaluacionTest {

    @Autowired
    private AsistenteIAService asistente;

    @Autowired
    private ConocimientoEscolar conocimiento;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Test
    void respuestaSobreElReglamentoEsRelevante() {
        String pregunta = "¿Cuál es la calificación mínima aprobatoria?";
        String respuesta = asistente.preguntar(pregunta, new UsernamePasswordAuthenticationToken(
                "evaluador", null, AuthorityUtils.createAuthorityList("ROLE_DIRECTOR")));

        EvaluationResponse evaluacion = RelevancyEvaluator.builder()
                .chatClientBuilder(chatClientBuilder)
                .build()
                .evaluate(new EvaluationRequest(pregunta,
                        List.of(new Document(conocimiento.consultarReglamento(pregunta))), respuesta));

        assertThat(respuesta).contains("6");
        assertThat(evaluacion.isPass()).as(evaluacion.getFeedback()).isTrue();
    }

    @Test
    void analisisEstructuradoDeUnAlumno() {
        AnalisisAlumno analisis = asistente.analizarAlumno("ESC-001");

        assertThat(analisis).isNotNull();
        assertThat(analisis.matricula()).isEqualTo("ESC-001");
        assertThat(analisis.nivelRiesgo()).isIn("BAJO", "MEDIO", "ALTO");
        assertThat(analisis.recomendaciones()).isNotEmpty();
    }
}
