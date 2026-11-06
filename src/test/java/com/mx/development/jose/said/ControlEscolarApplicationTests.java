package com.mx.development.jose.said;

import com.mx.development.jose.said.ai.AsistenteIAService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import static org.assertj.core.api.Assertions.assertThat;

/** La aplicación arranca sin clave de OpenAI y el asistente responde con un aviso. */
@SpringBootTest(properties = "spring.ai.openai.api-key=demo")
class ControlEscolarApplicationTests {

    @Autowired
    private AsistenteIAService asistente;

    @Test
    void arrancaSinClaveDeOpenAi() {
        Authentication usuario = new UsernamePasswordAuthenticationToken(
                "director", null, AuthorityUtils.createAuthorityList("ROLE_DIRECTOR"));

        assertThat(asistente.isConfigurado()).isFalse();
        assertThat(asistente.preguntar("hola", usuario)).startsWith("El asistente IA no está configurado");
        assertThat(asistente.preguntarStream("hola", usuario).collectList().block())
                .singleElement().asString().startsWith("El asistente IA no está configurado");
        assertThat(asistente.historial(usuario)).isEmpty();
    }
}
