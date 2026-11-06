# School Management System — Spring Boot + Spring AI + MCP

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?logo=springboot&logoColor=white) ![Spring AI](https://img.shields.io/badge/Spring_AI-2.0.1-6DB33F?logo=springboot&logoColor=white) ![OpenAI](https://img.shields.io/badge/OpenAI-gpt--4o--mini-412991?logo=openai&logoColor=white) ![MCP](https://img.shields.io/badge/MCP-Streamable_HTTP-1F2937) ![H2](https://img.shields.io/badge/H2-in--memory-09476B) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-optional-4169E1?logo=postgresql&logoColor=white)

**Project page:** https://josesaid.github.io/control-escolar-spring-ai-mcp/

## Overview
A school management web application ("Control Escolar") that covers students, teachers, subjects, classrooms, schedules, grades, reports, users, roles and permissions. On top of the classic CRUD it adds:

- an **AI assistant** built with the Spring AI `ChatClient` (chat memory, RAG, safeguards, streaming, structured output and tool calling), and
- an **MCP server** (Model Context Protocol, Streamable HTTP) that exposes the same school data as tools, resources, prompts and completions to any MCP client.

The user interface, domain names and sample data are in Spanish (the app targets a Mexican school); this README is in English.

Groups and enrollments exist in the model and are loaded with sample data, but they have no screens of their own yet.

## Stack
| Area | Technology |
|---|---|
| Language / build | Java 21, Maven, `jar` packaging, embedded Tomcat, port **8080** |
| Framework | Spring Boot **4.1.1** (Spring Framework 7): Web MVC, Data JPA, Security, Validation, Thymeleaf, Actuator |
| AI | Spring AI **2.0.1**: `spring-ai-starter-model-openai` (chat `gpt-4o-mini`, embeddings `text-embedding-3-small`), `spring-ai-starter-mcp-server-webmvc`, `spring-ai-starter-model-chat-memory-repository-jdbc`, `spring-ai-vector-store`, `spring-ai-vector-store-advisor` |
| Vector store | `SimpleVectorStore` (in memory, rebuilt at startup) |
| UI | Thymeleaf + `thymeleaf-extras-springsecurity6` + Bootstrap 5.3 |
| Database | H2 in memory (development); PostgreSQL driver included (profile commented out in `application.properties`) |
| Observability | Actuator (`health`, `info`, `metrics`, `prometheus`) + Micrometer Prometheus registry |
| Messaging | Not used |
| Cloud | Not used |
| Docker | Not used (no Dockerfile or Compose file) |

## Architecture
```
Browser (Thymeleaf)          MCP client (Inspector, Claude, IDE...)
        |                                  |
  controller/web  controller/api        /mcp (Streamable HTTP, SYNC)
        |               |                  |
        +------ service ------+     ai/EscolarTools  (@McpTool, @McpResource,
        |                     |                       @McpPrompt, @McpComplete)
   repository (JPA)     ai/AsistenteIAService (ChatClient + advisors)
        |                     |        |
      H2 / PostgreSQL         |   SimpleVectorStore (RAG over school rules)
                              |
                         OpenAI API
```
`EscolarTools` and `ConocimientoEscolar` are registered twice: as MCP tools for external clients and as Spring AI `@Tool` methods for the internal assistant, so both share one implementation.

## Components and classes
Root package: `com.mx.development.jose.said`

| Package | Classes |
|---|---|
| (root) | `ControlEscolarApplication` |
| `config` | `SecurityConfig` (form login, role rules, CSRF exceptions), `DataSeeder` (sample data), `WebConfig`, `EntityConverters` |
| `model` | `Usuario`, `Rol`, `Permiso`, `Alumno`, `Maestro`, `Materia`, `Aula`, `Grupo`, `Horario`, `Inscripcion`, `Calificacion`, `Asistencia` |
| `repository` | One `JpaRepository` per entity |
| `service` | `AlumnoService`, `MaestroService`, `MateriaService`, `AulaService`, `GrupoService`, `HorarioService`, `CalificacionService`, `UsuarioService`, `ReporteService`, `AsistenciaService` |
| `controller/web` | `Dashboard`, `Alumno`, `Maestro`, `Materia`, `Aula`, `Horario`, `Calificacion`, `Usuario`, `Rol`, `Reporte`, `Asistente` controllers |
| `controller/api` | `ApiAlumnoController`, `ApiCalificacionController`, `ApiAsistenteController` |
| `ai` | `EscolarTools` (read tools, MCP resources, prompts, completions), `EscolarEscrituraTools` (write tool with `ToolContext`), `ConocimientoEscolar` (RAG + `consultar-reglamento`), `AsistenteIAService` (ChatClient + advisors), `AnalisisAlumno` (structured output record), `ClaveOpenAi` (checks whether a real key is configured) |

Key terms: *alumno* = student, *maestro* = teacher, *materia* = subject, *aula* = classroom, *grupo* = class group, *horario* = schedule, *calificación* = grade, *asistencia* = attendance, *inscripción* = enrollment.

## Persistence
- H2 in memory (`jdbc:h2:mem:controlescolar`), recreated on every start (`ddl-auto=create-drop`) and filled by `DataSeeder`.
- H2 console: http://localhost:8080/h2-console (user `sa`, empty password).
- Chat memory is stored in the `SPRING_AI_CHAT_MEMORY` table of the same database (`initialize-schema=always`).
- PostgreSQL: uncomment the profile at the end of `application.properties` and set `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`.

Sample data:

| Entity | Data |
|---|---|
| Students | 8, IDs `ESC-001` .. `ESC-008` |
| Teachers | 5, employee numbers `EMP-001` .. `EMP-005` |
| Subjects | 6: `MAT101`, `ESP101`, `CIE101`, `HIS101`, `ING101`, `INF101` |
| Classrooms | 4: `A101`, `A102`, `LAB1`, `B201` |
| Groups | 3: `1A`, `2A`, `3A` |
| Schedules, grades, enrollments | term `2026-1` |
| Attendance | 10 days per student with 0–3 absences (some fall below 80 %) |
| Knowledge base (RAG) | `src/main/resources/documentos/reglamento-escolar.md`, `calendario-escolar.md` |

## Security
Form login (`/login`, `/logout`). Access is enforced by **role** in `SecurityConfig` and with `@PreAuthorize` on controllers:

| Module | Read | Create / edit / delete |
|---|---|---|
| Students, Teachers, Subjects, Classrooms, Schedules | All roles | ADMIN, DIRECTOR, SECRETARIA |
| Grades | ADMIN, DIRECTOR, MAESTRO, SECRETARIA | ADMIN, DIRECTOR, MAESTRO |
| Users, Roles and permissions | ADMIN, DIRECTOR | ADMIN, DIRECTOR |
| Dashboard, Reports, AI assistant | Any authenticated user | — |

Users also carry **permissions** (`LEER_ALUMNOS`, `CREAR_ALUMNOS`, `EDITAR_ALUMNOS`, `ELIMINAR_ALUMNOS`, `LEER_CALIFICACIONES`, `EDITAR_CALIFICACIONES`, `LEER_REPORTES`, `GESTION_USUARIOS`, `GESTION_HORARIOS`, `ASISTENTE_IA`). They are loaded as authorities, but no rule checks them yet: roles are what is enforced.

Demo users (local H2 sample data only — password = username + `123`): `admin`, `director`, `maestro`, `secretaria`, `alumno`.

> `/mcp` is public (`permitAll`) and excluded from CSRF so MCP clients can connect. Protect it (token or internal network) before any real deployment.

## MCP server
- Endpoint: `http://localhost:8080/mcp` (no login required)
- Server `control-escolar-mcp-server` v1.0.0, type SYNC, protocol STREAMABLE
- Try it with MCP Inspector: `npx @modelcontextprotocol/inspector` and connect to http://localhost:8080/mcp

| MCP tool | Parameters | Example |
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
| `top-alumnos` | `limite` (optional, default 5, max 50) | `3` |
| `consultar-reglamento` | `consulta` | `asistencia minima` |

- Resources: `escolar://materias`, `escolar://grupos`, `escolar://maestros` (JSON), `escolar://reglamento`, `escolar://calendario` (Markdown)
- Resource template: `escolar://alumnos/{matricula}` (student card in JSON with average and attendance)
- Prompts: `resumen-escolar`, `analisis-alumno(matricula)`, `plan-mejora-grupo(claveGrupo)`
- Completions (`@McpComplete`): `matricula` for `analisis-alumno` and `escolar://alumnos/{matricula}`, `claveGrupo` for `plan-mejora-grupo`

## AI assistant (ChatClient)
Page `/asistente`. With a valid `OPENAI_API_KEY` it uses:

| Spring AI component | Purpose |
|---|---|
| `MessageChatMemoryAdvisor` + `JdbcChatMemoryRepository` | Per-user memory; the "Nueva conversación" button clears it |
| `SafeGuardAdvisor` | Blocks questions containing `escolar.ia.palabras-bloqueadas`; runs before memory so blocked text is not stored |
| `QuestionAnswerAdvisor` + `SimpleVectorStore` | RAG over the school rules and calendar, indexed at startup |
| `SimpleLoggerAdvisor` | Logs requests/responses with `logging.level.org.springframework.ai.chat.client.advisor=DEBUG` |
| `.stream()` + SSE | Token-by-token answers (`GET /asistente/stream`) |
| `.entity(AnalisisAlumno.class)` | Structured output for `GET /api/ia/analisis-alumno/{matricula}` |
| `ToolContext` | Passes the user and roles to the `registrarCalificacion` tool |
| Micrometer observations | `gen_ai_client_*` metrics (tokens, latency) at `/actuator/prometheus` |

The **write tool `registrarCalificacion`** exists only in the chat, not in MCP (because `/mcp` has no authenticated user). Only ADMIN, DIRECTOR and MAESTRO can use it; the role is read from the `ToolContext`, which the model cannot change.

Without `OPENAI_API_KEY` (or with the default `demo`) the app still starts: the assistant answers with a notice, MCP tools keep working and `consultar-reglamento` falls back to keyword search instead of embeddings.

## Configuration
| Environment variable | Used in | Purpose |
|---|---|---|
| `OPENAI_API_KEY` | `application.properties` → `spring.ai.openai.api-key` | OpenAI chat and embeddings. Optional; defaults to `demo` (AI disabled) |
| `SPRING_DATASOURCE_USERNAME` | `application.properties` (PostgreSQL profile, commented) | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `application.properties` (PostgreSQL profile, commented) | Database password |

Other properties: `escolar.ia.palabras-bloqueadas` (blocked words for the SafeGuardAdvisor), `spring.ai.mcp.server.*` (MCP server name, version, type, protocol, capabilities).

## Running
Requirements: JDK 21+ and Maven 3.9+.
```bash
export OPENAI_API_KEY=<your key>   # optional
mvn spring-boot:run
# or
mvn clean package -DskipTests
java -jar target/control-escolar-1.0.0.jar
```
Open http://localhost:8080 (redirects to `/login`).

## Endpoints
**Web (Thymeleaf):** `/login`, `/` and `/dashboard`, `/alumnos`, `/maestros`, `/materias`, `/aulas`, `/horarios`, `/calificaciones`, `/reportes` (CSV export at `/reportes/exportar-csv`), `/usuarios`, `/roles`, `/asistente`, `/asistente/stream`.

**REST API** (JSON, requires a session; unauthenticated calls redirect to `/login`; CSRF disabled for `/api/**`):
- `GET /api/alumnos`, `GET /api/alumnos/{id}`, `GET /api/alumnos/matricula/{matricula}`, `GET /api/alumnos/grupo/{grupoId}`
- `GET /api/calificaciones`, `GET /api/calificaciones/alumno/{alumnoId}`, `GET /api/calificaciones/alumno/{alumnoId}/promedio`, `GET /api/calificaciones/materia/{materiaId}/promedio`
- `POST /api/ia/preguntar` with `{"pregunta": "..."}` (uses the session user's memory)
- `GET /api/ia/analisis-alumno/{matricula}` → `AnalisisAlumno` (average, attendance, risk level, strengths, improvement areas, recommendations); `503` without an OpenAI key

**MCP:** `/mcp`. **Actuator:** `/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/prometheus`.

## Tests
```bash
mvn test
```
Unit tests cover the read tools (`EscolarToolsTest`), the write tool and its role checks (`EscolarEscrituraToolsTest`), the knowledge base fallback (`ConocimientoEscolarTest`) and startup without a key (`ControlEscolarApplicationTests`). `AsistenteIAEvaluacionTest` (`RelevancyEvaluator` + structured output) runs only when `OPENAI_API_KEY` starts with `sk-`, and it consumes tokens.

## Known limitations
- **Attendance:** sample data and an AI tool exist, but there is no controller or view yet.
- **Groups and enrollments:** loaded by `DataSeeder`; no web CRUD.
- **RAG:** `SimpleVectorStore` lives in memory and is re-indexed on every start; PGVector is a better fit for production.
- **Permissions** are loaded but not enforced; access control is role-based.
- `/mcp` is unauthenticated.
- `WebConfig` and `SecurityConfig` map `/js/**`, but `static/js` does not exist yet.
- No Docker or CI configuration.
