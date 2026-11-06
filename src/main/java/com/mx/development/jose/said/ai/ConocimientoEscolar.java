package com.mx.development.jose.said.ai;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Base de conocimiento para RAG: el reglamento y el calendario escolar
 * ({@code classpath:documentos/*.md}), divididos por sección ({@code ## }).
 *
 * Si hay una clave de OpenAI válida, las secciones se indexan en un
 * {@link SimpleVectorStore} (embeddings en memoria) que usa el
 * {@code QuestionAnswerAdvisor} del asistente. Sin clave, o si la
 * indexación falla, {@link #getVectorStore()} devuelve {@code null} y la
 * tool {@code consultar-reglamento} busca por palabras clave, así que la
 * aplicación arranca y la tool MCP sigue funcionando.
 */
@Component
public class ConocimientoEscolar {

    private static final Logger log = LoggerFactory.getLogger(ConocimientoEscolar.class);

    public static final List<String> TOOL_NAMES = List.of("consultar-reglamento");

    private static final String PATRON_DOCUMENTOS = "classpath:documentos/*.md";
    private static final int MAX_RESULTADOS = 3;

    private final ObjectProvider<EmbeddingModel> embeddingModelProvider;
    private final Environment environment;

    private final Map<String, String> documentosCompletos = new LinkedHashMap<>();
    private final List<Document> secciones = new ArrayList<>();
    private VectorStore vectorStore;

    public ConocimientoEscolar(ObjectProvider<EmbeddingModel> embeddingModelProvider, Environment environment) {
        this.embeddingModelProvider = embeddingModelProvider;
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        cargarDocumentos();
        if (secciones.isEmpty() || !ClaveOpenAi.esValida(environment)) {
            return;
        }
        try {
            EmbeddingModel embeddingModel = embeddingModelProvider.getIfAvailable();
            if (embeddingModel == null) {
                return;
            }
            SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
            store.add(secciones);
            vectorStore = store;
            log.info("Base de conocimiento indexada: {} secciones", secciones.size());
        } catch (Exception e) {
            vectorStore = null;
            log.warn("No se pudo indexar la base de conocimiento; se usará búsqueda por palabras: {}", e.getMessage());
        }
    }

    /** Vector store con el reglamento y el calendario, o {@code null} si no se pudo indexar. */
    public VectorStore getVectorStore() {
        return vectorStore;
    }

    @Tool(description = "Busca en el reglamento y el calendario escolar: calificación mínima, asistencia, "
            + "extraordinarios, bajas, fechas de parciales, vacaciones e inscripciones.")
    @McpTool(name = "consultar-reglamento",
            description = "Busca en el reglamento y el calendario escolar: calificación mínima, asistencia, "
                    + "extraordinarios, bajas, fechas de parciales, vacaciones e inscripciones.")
    public String consultarReglamento(
            @ToolParam(description = "Tema o pregunta a buscar")
            @McpToolParam(description = "Tema o pregunta a buscar") String consulta) {
        if (consulta == null || consulta.isBlank()) {
            return "Indica el tema a buscar en el reglamento o el calendario escolar.";
        }
        List<Document> encontrados = buscar(consulta);
        if (encontrados.isEmpty()) {
            return "No encontré información sobre \"" + consulta + "\" en el reglamento ni en el calendario escolar.";
        }
        StringBuilder sb = new StringBuilder();
        for (Document d : encontrados) {
            if (!sb.isEmpty()) {
                sb.append("\n\n");
            }
            sb.append("[").append(d.getMetadata().get("fuente")).append(" - ")
                    .append(d.getMetadata().get("seccion")).append("]\n").append(d.getText());
        }
        return sb.toString();
    }

    @McpResource(uri = "escolar://reglamento",
            name = "reglamento-escolar",
            description = "Reglamento escolar completo en Markdown",
            mimeType = "text/markdown")
    public String reglamentoResource() {
        return documentosCompletos.getOrDefault("reglamento-escolar", "Reglamento no disponible.");
    }

    @McpResource(uri = "escolar://calendario",
            name = "calendario-escolar",
            description = "Calendario escolar completo en Markdown",
            mimeType = "text/markdown")
    public String calendarioResource() {
        return documentosCompletos.getOrDefault("calendario-escolar", "Calendario no disponible.");
    }

    private List<Document> buscar(String consulta) {
        if (vectorStore != null) {
            try {
                return vectorStore.similaritySearch(SearchRequest.builder()
                        .query(consulta).topK(MAX_RESULTADOS).similarityThreshold(0.3).build());
            } catch (Exception e) {
                log.warn("Falló la búsqueda semántica; se usará búsqueda por palabras: {}", e.getMessage());
            }
        }
        return buscarPorPalabras(consulta);
    }

    /** Puntúa cada sección por cuántas palabras (de 4 letras o más) de la consulta contiene. */
    private List<Document> buscarPorPalabras(String consulta) {
        List<String> palabras = Arrays.stream(normalizar(consulta).split("[^\\p{L}\\p{N}]+"))
                .filter(p -> p.length() >= 4)
                .toList();
        if (palabras.isEmpty()) {
            return List.of();
        }
        return secciones.stream()
                .map(d -> Map.entry(d, palabras.stream().filter(normalizar(d.getText())::contains).count()))
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<Document, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(MAX_RESULTADOS)
                .map(Map.Entry::getKey)
                .toList();
    }

    private void cargarDocumentos() {
        try {
            Resource[] recursos = new PathMatchingResourcePatternResolver().getResources(PATRON_DOCUMENTOS);
            for (Resource recurso : recursos) {
                String nombre = recurso.getFilename() == null ? "documento" : recurso.getFilename().replace(".md", "");
                String texto = recurso.getContentAsString(StandardCharsets.UTF_8);
                documentosCompletos.put(nombre, texto);
                for (String bloque : texto.split("(?m)^## ")) {
                    if (bloque.isBlank() || bloque.startsWith("# ")) {
                        continue;
                    }
                    String seccion = bloque.lines().findFirst().orElse("").trim();
                    secciones.add(new Document("## " + bloque.trim(), Map.of("fuente", nombre, "seccion", seccion)));
                }
            }
        } catch (Exception e) {
            log.warn("No se pudieron leer los documentos de {}: {}", PATRON_DOCUMENTOS, e.getMessage());
        }
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
