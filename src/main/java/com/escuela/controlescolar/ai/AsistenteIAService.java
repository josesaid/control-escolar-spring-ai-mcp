package com.escuela.controlescolar.ai;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Asistente de IA del sistema de control escolar.
 *
 * El {@link ChatClient} se construye de forma perezosa en {@code @PostConstruct}
 * dentro de un try/catch: si no hay un builder disponible, si la clave de
 * OpenAI no es real (ausente o con el valor de ejemplo {@code "demo"}) o si
 * la construcción falla, el cliente queda en {@code null} y la aplicación
 * arranca igualmente. En ese caso los métodos devuelven un mensaje
 * explicando cómo configurarlo, sin lanzar excepciones.
 * No se usa ningún {@code @Value} obligatorio para la clave.
 *
 * Componentes de Spring AI que usa el cliente del chat:
 * <ul>
 *   <li>{@link MessageChatMemoryAdvisor}: memoria por conversación (JDBC, tabla
 *       {@code SPRING_AI_CHAT_MEMORY}); el id de conversación es el usuario.</li>
 *   <li>{@link SafeGuardAdvisor}: bloquea preguntas con palabras sensibles
 *       ({@code escolar.ia.palabras-bloqueadas}).</li>
 *   <li>{@link QuestionAnswerAdvisor}: RAG sobre el reglamento y el calendario
 *       (solo si {@link ConocimientoEscolar} pudo indexarlos).</li>
 *   <li>{@link SimpleLoggerAdvisor}: registra peticiones y respuestas en DEBUG.</li>
 *   <li>{@code toolContext}: pasa usuario y roles a {@link EscolarEscrituraTools}.</li>
 * </ul>
 */
@Service
public class AsistenteIAService {

    private static final String SIN_CONFIGURACION = "El asistente IA no está configurado: "
            + "define la variable de entorno OPENAI_API_KEY con una clave válida y reinicia. "
            + "Mientras tanto puedes usar las tools MCP y los reportes.";

    private static final String SISTEMA = "Eres un asistente escolar del sistema de control escolar. "
            + "Responde siempre en español, de forma clara y breve. "
            + "Usa las tools disponibles para consultar datos reales "
            + "(alumnos, calificaciones, asistencia, horarios, maestros, aulas, reportes y reglamento) "
            + "antes de responder, y si no tienes el dato, dilo claramente en lugar de inventarlo. "
            + "Antes de registrar una calificación, confirma los datos con el usuario.";

    private static final String SISTEMA_ANALISIS = "Eres un orientador escolar. "
            + "Usa las tools para consultar los datos reales del alumno (datos generales, calificaciones "
            + "y asistencia) y devuelve un análisis en español. nivelRiesgo debe ser BAJO, MEDIO o ALTO: "
            + "ALTO si el promedio es menor a 6.0 o la asistencia menor a 80 %, MEDIO si el promedio es "
            + "menor a 7.5, BAJO en otro caso. Fortalezas, áreas de oportunidad y recomendaciones deben "
            + "basarse solo en los datos que devuelven las tools (calificaciones, materias y asistencia); "
            + "no inventes datos como participación o conducta. Incluye siempre al menos una fortaleza, "
            + "un área de oportunidad y dos recomendaciones concretas.";

    /**
     * Plantilla del QuestionAnswerAdvisor. La de Spring AI pide responder solo con el
     * contexto recuperado, lo que impedía usar las tools; aquí el contexto es una referencia más.
     */
    private static final String PLANTILLA_RAG = """
            {query}

            Fragmentos del reglamento y el calendario escolar que podrían ser útiles:
            ---------------------
            {question_answer_context}
            ---------------------

            Usa estos fragmentos solo si la pregunta trata de normas o fechas escolares.
            Para datos de alumnos, maestros, grupos o calificaciones, o para registrar
            información, usa las tools disponibles como siempre.
            """;

    private static final String PALABRAS_BLOQUEADAS_POR_DEFECTO = "contraseña,password,tarjeta de crédito,CURP";

    private final EscolarTools escolarTools;
    private final EscolarEscrituraTools escrituraTools;
    private final ConocimientoEscolar conocimiento;
    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final ObjectProvider<ChatMemory> chatMemoryProvider;
    private final Environment environment;

    private ChatClient chatClient;
    private ChatClient analisisClient;
    private ChatMemory chatMemory;

    public AsistenteIAService(EscolarTools escolarTools,
                              EscolarEscrituraTools escrituraTools,
                              ConocimientoEscolar conocimiento,
                              ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
                              ObjectProvider<ChatMemory> chatMemoryProvider,
                              Environment environment) {
        this.escolarTools = escolarTools;
        this.escrituraTools = escrituraTools;
        this.conocimiento = conocimiento;
        this.chatClientBuilderProvider = chatClientBuilderProvider;
        this.chatMemoryProvider = chatMemoryProvider;
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        chatMemory = chatMemoryProvider.getIfAvailable(() -> MessageWindowChatMemory.builder().build());
        try {
            if (!ClaveOpenAi.esValida(environment)) {
                chatClient = null;
                return;
            }
            ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
            if (builder == null) {
                chatClient = null;
                return;
            }
            ChatClient.Builder analisisBuilder = builder.clone();
            chatClient = builder
                    .defaultSystem(SISTEMA)
                    .defaultTools(escolarTools, escrituraTools, conocimiento)
                    .defaultAdvisors(advisorsDelChat())
                    .build();
            // Sin memoria ni RAG: cada análisis es independiente de la conversación del usuario.
            analisisClient = analisisBuilder
                    .defaultSystem(SISTEMA_ANALISIS)
                    .defaultTools(escolarTools)
                    .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                    .build();
        } catch (Exception e) {
            chatClient = null;
            analisisClient = null;
        }
    }

    public boolean isConfigurado() {
        return chatClient != null;
    }

    public String preguntar(String pregunta) {
        return preguntar(pregunta, null);
    }

    public String preguntar(String pregunta, Authentication autenticacion) {
        if (pregunta == null || pregunta.isBlank()) {
            return "Escribe una pregunta para el asistente escolar.";
        }
        if (chatClient == null) {
            return SIN_CONFIGURACION + " Tu pregunta fue: " + pregunta;
        }
        try {
            String contenido = peticion(pregunta, autenticacion).call().content();
            if (contenido == null || contenido.isBlank()) {
                return "El modelo de IA no devolvió una respuesta. Tu pregunta fue: " + pregunta;
            }
            return contenido;
        } catch (Exception e) {
            return mensajeError(e, pregunta);
        }
    }

    /** Igual que {@link #preguntar(String, Authentication)}, pero devuelve la respuesta token por token. */
    public Flux<String> preguntarStream(String pregunta, Authentication autenticacion) {
        if (pregunta == null || pregunta.isBlank()) {
            return Flux.just("Escribe una pregunta para el asistente escolar.");
        }
        if (chatClient == null) {
            return Flux.just(SIN_CONFIGURACION + " Tu pregunta fue: " + pregunta);
        }
        try {
            return peticion(pregunta, autenticacion).stream().content()
                    .onErrorResume(e -> Flux.just(mensajeError(e, pregunta)));
        } catch (Exception e) {
            return Flux.just(mensajeError(e, pregunta));
        }
    }

    /**
     * Análisis estructurado de un alumno (structured output).
     * Devuelve {@code null} si el asistente no está configurado.
     */
    public AnalisisAlumno analizarAlumno(String matricula) {
        if (analisisClient == null) {
            return null;
        }
        return analisisClient.prompt()
                .user("Analiza al alumno con matrícula " + matricula + ".")
                .call()
                .entity(AnalisisAlumno.class);
    }

    /** Mensajes de usuario y asistente guardados en la memoria de la conversación. */
    public List<Message> historial(Authentication autenticacion) {
        return chatMemory.get(conversacionId(autenticacion)).stream()
                .filter(m -> m.getMessageType() == MessageType.USER || m.getMessageType() == MessageType.ASSISTANT)
                .toList();
    }

    public void limpiarConversacion(Authentication autenticacion) {
        chatMemory.clear(conversacionId(autenticacion));
    }

    private ChatClient.ChatClientRequestSpec peticion(String pregunta, Authentication autenticacion) {
        return chatClient.prompt()
                .user(pregunta)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversacionId(autenticacion)))
                .toolContext(Map.of(
                        EscolarEscrituraTools.CTX_USUARIO, conversacionId(autenticacion),
                        EscolarEscrituraTools.CTX_ROLES, roles(autenticacion)));
    }

    private List<Advisor> advisorsDelChat() {
        List<Advisor> advisors = new ArrayList<>();
        advisors.add(SafeGuardAdvisor.builder()
                .sensitiveWords(palabrasBloqueadas())
                .failureResponse("Por seguridad no puedo responder preguntas sobre datos sensibles "
                        + "(contraseñas, datos bancarios o documentos personales).")
                // Antes que la memoria: así lo bloqueado no se guarda en el historial
                // (si se guardara, el SafeGuard bloquearía el resto de la conversación).
                .order(Ordered.HIGHEST_PRECEDENCE)
                .build());
        advisors.add(MessageChatMemoryAdvisor.builder(chatMemory).build());
        if (conocimiento.getVectorStore() != null) {
            advisors.add(QuestionAnswerAdvisor.builder(conocimiento.getVectorStore())
                    .searchRequest(SearchRequest.builder().topK(3).similarityThreshold(0.5).build())
                    .promptTemplate(new PromptTemplate(PLANTILLA_RAG))
                    .build());
        }
        advisors.add(SimpleLoggerAdvisor.builder().build());
        return advisors;
    }

    private List<String> palabrasBloqueadas() {
        String valor = environment.getProperty("escolar.ia.palabras-bloqueadas", PALABRAS_BLOQUEADAS_POR_DEFECTO);
        return Arrays.stream(valor.split(","))
                .map(String::trim)
                .filter(p -> !p.isEmpty())
                .toList();
    }

    private String conversacionId(Authentication autenticacion) {
        return autenticacion == null || autenticacion.getName() == null ? "anonimo" : autenticacion.getName();
    }

    private List<String> roles(Authentication autenticacion) {
        if (autenticacion == null) {
            return List.of();
        }
        return autenticacion.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }

    private String mensajeError(Throwable e, String pregunta) {
        return "No pude consultar el modelo de IA (" + e.getMessage() + "). "
                + "Revisa que la variable de entorno OPENAI_API_KEY tenga una clave válida "
                + "y vuelve a intentarlo. Tu pregunta fue: " + pregunta;
    }
}
