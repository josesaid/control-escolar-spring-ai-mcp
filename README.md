# Control Escolar — Spring Boot + Spring AI + MCP

Aplicacion de control escolar (alumnos, maestros, materias, aulas, horarios, calificaciones, reportes, usuarios, roles y permisos) con asistente de IA y servidor MCP. Los grupos y las inscripciones existen en el modelo y se cargan con datos de ejemplo, pero no tienen pantallas propias.

## Stack
- Java 21, Maven, packaging `jar`, Tomcat embebido, puerto **8080**
- Spring Boot **4.1.1** (Spring Framework 7)
- Spring AI **2.0.1** — `spring-ai-starter-mcp-server-webmvc` (Streamable HTTP), `spring-ai-starter-model-openai` (gpt-4o-mini y `text-embedding-3-small`), `spring-ai-starter-model-chat-memory-repository-jdbc` (memoria), `spring-ai-vector-store` + `spring-ai-vector-store-advisor` (RAG)
- Spring Data JPA + Spring Security + Thymeleaf + Bootstrap 5.3
- Spring Boot Actuator (`/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/prometheus`) + Micrometer Prometheus
- H2 en memoria para desarrollo; driver PostgreSQL incluido (perfil comentado en `application.properties`)

## Requisitos
- JDK 21 y Maven 3.9+ instalados (`java -version`, `mvn -version`).
- Sin `OPENAI_API_KEY` (o con el valor por defecto `demo`) la app arranca igual: el asistente responde con un aviso, las tools MCP siguen funcionando y `consultar-reglamento` busca por palabras clave en lugar de usar embeddings.

## Como correr
```bash
mvn spring-boot:run
# o bien:
mvn clean package -DskipTests
java -jar target/control-escolar-1.0.0.jar
```
Abre http://localhost:8080 — te lleva a `/login`.

Para habilitar el asistente IA:
```bash
export OPENAI_API_KEY=sk-...
mvn spring-boot:run
```

## Usuarios de ejemplo (contrasena = usuario + 123)
| Usuario | Contrasena | Rol |
|---|---|---|
| admin | admin123 | ADMIN |
| director | director123 | DIRECTOR |
| maestro | maestro123 | MAESTRO |
| secretaria | secretaria123 | SECRETARIA |
| alumno | alumno123 | ALUMNO |

## Datos de ejemplo
La base H2 se recrea en cada arranque (`ddl-auto=create-drop`) y `DataSeeder` la llena con:

| Entidad | Datos |
|---|---|
| Alumnos | 8, matriculas `ESC-001` .. `ESC-008` |
| Maestros | 5, numeros de empleado `EMP-001` .. `EMP-005` |
| Materias | 6: `MAT101`, `ESP101`, `CIE101`, `HIS101`, `ING101`, `INF101` |
| Aulas | 4: `A101`, `A102`, `LAB1`, `B201` |
| Grupos | 3: `1A`, `2A`, `3A` |
| Horarios, calificaciones e inscripciones | periodo `2026-1` |
| Asistencias | 10 dias por alumno, con 0 a 3 faltas (algunos quedan bajo el 80 %) |
| Reglamento y calendario | `src/main/resources/documentos/*.md` (base de conocimiento para RAG) |

Consola H2: http://localhost:8080/h2-console (JDBC `jdbc:h2:mem:controlescolar`, usuario `sa`, sin contrasena).

## Modulos
Dashboard, Alumnos, Maestros, Materias, Aulas, Horarios, Calificaciones, Reportes (con exportar CSV en `/reportes/exportar-csv`), Usuarios, Roles y permisos, Asistente IA.

## Seguridad: roles y acceso
Login por formulario (`/login`, `/logout`). El acceso se controla por **rol** con `SecurityConfig` y `@PreAuthorize` en los controladores:

| Modulo | Consultar | Crear / editar / eliminar |
|---|---|---|
| Alumnos, Maestros, Materias, Aulas, Horarios | Todos los roles | ADMIN, DIRECTOR, SECRETARIA |
| Calificaciones | ADMIN, DIRECTOR, MAESTRO, SECRETARIA | ADMIN, DIRECTOR, MAESTRO |
| Usuarios, Roles y permisos | ADMIN, DIRECTOR | ADMIN, DIRECTOR |
| Dashboard, Reportes, Asistente IA | Cualquier usuario autenticado | — |

Ademas cada usuario tiene **permisos** (`LEER_ALUMNOS`, `CREAR_ALUMNOS`, `EDITAR_ALUMNOS`, `ELIMINAR_ALUMNOS`, `LEER_CALIFICACIONES`, `EDITAR_CALIFICACIONES`, `LEER_REPORTES`, `GESTION_USUARIOS`, `GESTION_HORARIOS`, `ASISTENTE_IA`). Se cargan como authorities de Spring Security, pero hoy ninguna regla los verifica: el control efectivo es por rol.

> **Nota:** `/mcp` es publico (`permitAll`) y sin CSRF para que los clientes MCP puedan conectarse. En produccion debe protegerse (token o red interna).

## MCP server (Spring AI 2.0, Streamable HTTP)
- Endpoint: `http://localhost:8080/mcp` (no requiere login)
- Servidor: `control-escolar-mcp-server` v1.0.0, tipo SYNC, protocolo STREAMABLE
- Tools:

| Tool MCP | Parametro | Ejemplo |
|---|---|---|
| `consultar-alumno-por-matricula` | `matricula` | `ESC-001` |
| `promedio-alumno` | `matricula` | `ESC-004` |
| `calificaciones-por-materia` | `claveMateria` | `MAT101` |
| `listar-horario-grupo` | `claveGrupo` | `1A` |
| `buscar-maestro` | `numeroEmpleado` | `EMP-001` |
| `generar-reporte-grupo` | `claveGrupo` | `2A` |
| `listar-alumnos-en-riesgo` | — | — |
| `porcentaje-asistencia-alumno` | `matricula` | `ESC-004` |
| `calificaciones-alumno-periodo` | `matricula`, `periodo` | `ESC-001`, `2026-1` |
| `horario-maestro` | `numeroEmpleado` | `EMP-001` |
| `listar-aulas` | — | — |
| `promedio-por-materia` | — | — |
| `promedio-por-grupo` | — | — |
| `resumen-general` | — | — |
| `top-alumnos` | `limite` (opcional, 5 por defecto, max 50) | `3` |
| `consultar-reglamento` | `consulta` | `asistencia minima` |

- Recursos: `escolar://materias`, `escolar://grupos`, `escolar://maestros` (JSON), `escolar://reglamento`, `escolar://calendario` (Markdown)
- Plantilla de recurso: `escolar://alumnos/{matricula}` (ficha JSON con promedio y porcentaje de asistencia)
- Prompts: `resumen-escolar`, `analisis-alumno(matricula)`, `plan-mejora-grupo(claveGrupo)`
- Completions (`@McpComplete`): autocompletan `matricula` en `analisis-alumno` y en `escolar://alumnos/{matricula}`, y `claveGrupo` en `plan-mejora-grupo`
- Prueba con MCP Inspector: `npx @modelcontextprotocol/inspector` y conecta a http://localhost:8080/mcp

Los mismos metodos de `EscolarTools` y `ConocimientoEscolar` se registran tambien como `@Tool` de Spring AI para el asistente interno (ChatClient); ahi se exponen con el nombre del metodo Java (`consultarAlumnoPorMatricula`, `promedioAlumno`, etc.).

## Asistente IA (ChatClient)
Pagina `/asistente`. Con `OPENAI_API_KEY` valida usa:

| Componente de Spring AI | Uso |
|---|---|
| `MessageChatMemoryAdvisor` + `JdbcChatMemoryRepository` | Memoria por usuario en la tabla `SPRING_AI_CHAT_MEMORY`; boton "Nueva conversacion" la borra |
| `SafeGuardAdvisor` | Bloquea preguntas con `escolar.ia.palabras-bloqueadas`; corre antes que la memoria para no guardar lo bloqueado |
| `QuestionAnswerAdvisor` + `SimpleVectorStore` | RAG sobre el reglamento y el calendario (indexados al arrancar) |
| `SimpleLoggerAdvisor` | Peticiones y respuestas en el log con `logging.level.org.springframework.ai.chat.client.advisor=DEBUG` |
| `.stream()` + SSE | La pagina muestra la respuesta token por token (`GET /asistente/stream`) |
| `.entity(AnalisisAlumno.class)` | Structured output en `GET /api/ia/analisis-alumno/{matricula}` |
| `ToolContext` | Pasa usuario y roles a la tool `registrarCalificacion` |
| Observabilidad (Micrometer) | Metricas `gen_ai_client_*` (tokens, latencia) en `/actuator/prometheus` |

**Tool de escritura `registrarCalificacion`:** solo existe en el chat (no en MCP, porque `/mcp` no tiene usuario autenticado). Solo la pueden usar ADMIN, DIRECTOR y MAESTRO; el rol se lee del `ToolContext`, que el modelo no puede alterar.

## API REST
Solo lectura (JSON). **Requiere sesion iniciada**: sin autenticacion la respuesta es una redireccion a `/login`. CSRF esta deshabilitado para `/api/**`.

- `GET /api/alumnos`, `GET /api/alumnos/{id}`, `GET /api/alumnos/matricula/{matricula}`, `GET /api/alumnos/grupo/{grupoId}`
- `GET /api/calificaciones`, `GET /api/calificaciones/alumno/{alumnoId}`, `GET /api/calificaciones/alumno/{alumnoId}/promedio`, `GET /api/calificaciones/materia/{materiaId}/promedio`
- `POST /api/ia/preguntar` con `{"pregunta": "..."}` (usa la memoria del usuario de la sesion)
- `GET /api/ia/analisis-alumno/{matricula}` → `AnalisisAlumno` (promedio, asistencia, nivel de riesgo, fortalezas, areas de oportunidad, recomendaciones); `503` sin clave de OpenAI

## Estructura
```
src/main/java/com/escuela/controlescolar/
  ControlEscolarApplication.java
  config/  SecurityConfig, DataSeeder, WebConfig, EntityConverters
  model/   Usuario, Rol, Permiso, Alumno, Maestro, Materia, Aula, Grupo, Horario, Inscripcion, Calificacion, Asistencia
  repository/  JpaRepository por entidad
  service/ AlumnoService, MaestroService, MateriaService, AulaService, GrupoService, HorarioService,
           CalificacionService, UsuarioService, ReporteService, AsistenciaService
  controller/web/  Dashboard, Alumno, Maestro, Materia, Aula, Horario, Calificacion, Usuario, Rol, Reporte, Asistente
  controller/api/  ApiAlumno, ApiCalificacion, ApiAsistente
  ai/      EscolarTools (@Tool + @McpTool, @McpResource, @McpPrompt, @McpComplete),
           EscolarEscrituraTools (@Tool con ToolContext), ConocimientoEscolar (RAG + consultar-reglamento),
           AsistenteIAService (ChatClient + advisors), AnalisisAlumno (structured output)
src/main/resources/application.properties
src/main/resources/documentos/  reglamento-escolar.md, calendario-escolar.md
src/main/resources/templates/  layout, login y vistas Thymeleaf por modulo
src/main/resources/static/css/style.css
```

## Estado actual y pendientes
- **Asistencia:** hay datos de ejemplo y tool de IA, pero todavia no hay controlador ni vistas.
- **Grupos e inscripciones:** se cargan desde `DataSeeder`; no hay CRUD web para ellos.
- **Pruebas:** `mvn test` corre las pruebas unitarias de las tools y el arranque sin clave. `AsistenteIAEvaluacionTest` (`RelevancyEvaluator` + structured output) solo corre si `OPENAI_API_KEY` empieza con `sk-` y consume tokens.
- **RAG:** `SimpleVectorStore` vive en memoria y se reindexa en cada arranque; en produccion conviene PGVector.
- `WebConfig` y `SecurityConfig` mapean `/js/**`, pero la carpeta `static/js` todavia no existe.
