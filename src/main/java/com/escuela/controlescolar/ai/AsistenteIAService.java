package com.escuela.controlescolar.ai;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Asistente de IA del sistema de control escolar.
 *
 * El {@link ChatClient} se construye de forma perezosa en {@code @PostConstruct}
 * dentro de un try/catch: si no hay un builder disponible, si la clave de
 * OpenAI no es real (ausente o con el valor de ejemplo {@code "demo"}) o si
 * la construcción falla, el cliente queda en {@code null} y la aplicación
 * arranca igualmente. En ese caso {@link #preguntar(String)} devuelve un
 * mensaje explicando cómo configurarlo, sin lanzar excepciones.
 * No se usa ningún {@code @Value} obligatorio para la clave.
 */
@Service
public class AsistenteIAService {

    private static final String SIN_CONFIGURACION = "El asistente IA no está configurado: "
            + "define la variable de entorno OPENAI_API_KEY con una clave válida y reinicia. "
            + "Mientras tanto puedes usar las tools MCP y los reportes.";

    private final EscolarTools escolarTools;
    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final Environment environment;

    private ChatClient chatClient;

    public AsistenteIAService(EscolarTools escolarTools,
                              ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
                              Environment environment) {
        this.escolarTools = escolarTools;
        this.chatClientBuilderProvider = chatClientBuilderProvider;
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        try {
            String apiKey = environment.getProperty("spring.ai.openai.api-key");
            if (apiKey == null || apiKey.isBlank() || "demo".equalsIgnoreCase(apiKey.trim())) {
                chatClient = null;
                return;
            }
            ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
            if (builder == null) {
                chatClient = null;
                return;
            }
            chatClient = builder
                    .defaultSystem("Eres un asistente escolar del sistema de control escolar. "
                            + "Responde siempre en español, de forma clara y breve. "
                            + "Usa las tools disponibles para consultar datos reales "
                            + "(alumnos, calificaciones, horarios, maestros y reportes) antes de responder, "
                            + "y si no tienes el dato, dilo claramente en lugar de inventarlo.")
                    .defaultTools(escolarTools)
                    .build();
        } catch (Exception e) {
            chatClient = null;
        }
    }

    public String preguntar(String pregunta) {
        if (pregunta == null || pregunta.isBlank()) {
            return "Escribe una pregunta para el asistente escolar.";
        }
        if (chatClient == null) {
            return SIN_CONFIGURACION + " Tu pregunta fue: " + pregunta;
        }
        try {
            String contenido = chatClient.prompt().user(pregunta).call().content();
            if (contenido == null || contenido.isBlank()) {
                return "El modelo de IA no devolvió una respuesta. Tu pregunta fue: " + pregunta;
            }
            return contenido;
        } catch (Exception e) {
            return "No pude consultar el modelo de IA (" + e.getMessage() + "). "
                    + "Revisa que la variable de entorno OPENAI_API_KEY tenga una clave válida "
                    + "y vuelve a intentarlo. Tu pregunta fue: " + pregunta;
        }
    }
}
