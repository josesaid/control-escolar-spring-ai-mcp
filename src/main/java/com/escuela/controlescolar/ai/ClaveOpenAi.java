package com.escuela.controlescolar.ai;

import org.springframework.core.env.Environment;

/**
 * Indica si la clave de OpenAI configurada es real: ni ausente, ni vacía,
 * ni el valor de ejemplo {@code "demo"} que trae {@code application.properties}.
 */
final class ClaveOpenAi {

    private ClaveOpenAi() {
    }

    static boolean esValida(Environment environment) {
        String apiKey = environment.getProperty("spring.ai.openai.api-key");
        return apiKey != null && !apiKey.isBlank() && !"demo".equalsIgnoreCase(apiKey.trim());
    }
}
