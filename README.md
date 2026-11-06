# Control Escolar — Spring Boot + Spring AI + MCP

Aplicacion de control escolar (alumnos, maestros, materias, aulas, horarios, calificaciones, reportes, usuarios, roles y permisos) con asistente de IA y servidor MCP. Los grupos y las inscripciones existen en el modelo y se cargan con datos de ejemplo, pero no tienen pantallas propias.

## Stack
- Java 21, Maven, packaging `jar`, Tomcat embebido, puerto **8080**
- Spring Boot **4.1.1** (Spring Framework 7)
- Spring AI **2.0.1** — `spring-ai-starter-mcp-server-webmvc` (Streamable HTTP) y `spring-ai-starter-model-openai` (gpt-4o-mini)
- Spring Data JPA + Spring Security + Thymeleaf + Bootstrap 5.3
- Spring Boot Actuator (`/actuator/health`, `/actuator/info`)
- H2 en memoria para desarrollo; driver PostgreSQL incluido (perfil comentado en `application.properties`)

## Requisitos
- JDK 21 y Maven 3.9+ instalados (`java -version`, `mvn -version`).
- Sin `OPENAI_API_KEY` (o con el valor por defecto `demo`) la app arranca igual: el asistente responde con un aviso y las tools MCP siguen funcionando.

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

- Recurso: `escolar://materias` (JSON con clave y nombre) — Prompt: `resumen-escolar`
- Prueba con MCP Inspector: `npx @modelcontextprotocol/inspector` y conecta a http://localhost:8080/mcp

Los mismos metodos de `EscolarTools` se registran tambien como `@Tool` de Spring AI para el asistente interno (ChatClient); ahi se exponen con el nombre del metodo Java (`consultarAlumnoPorMatricula`, `promedioAlumno`, etc.).

## API REST
Solo lectura (JSON). **Requiere sesion iniciada**: sin autenticacion la respuesta es una redireccion a `/login`. CSRF esta deshabilitado para `/api/**`.

- `GET /api/alumnos`, `GET /api/alumnos/{id}`, `GET /api/alumnos/matricula/{matricula}`, `GET /api/alumnos/grupo/{grupoId}`
- `GET /api/calificaciones`, `GET /api/calificaciones/alumno/{alumnoId}`, `GET /api/calificaciones/alumno/{alumnoId}/promedio`, `GET /api/calificaciones/materia/{materiaId}/promedio`

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
  controller/api/  ApiAlumno, ApiCalificacion
  ai/      EscolarTools (@Tool + @McpTool, @McpResource, @McpPrompt), AsistenteIAService (ChatClient)
src/main/resources/application.properties
src/main/resources/templates/  layout, login y vistas Thymeleaf por modulo
src/main/resources/static/css/style.css
```

## Estado actual y pendientes
- **Asistencia:** existen la entidad, el repositorio y `AsistenciaService`, pero todavia no hay controlador, vistas ni datos de ejemplo.
- **Grupos e inscripciones:** se cargan desde `DataSeeder`; no hay CRUD web para ellos.
- **Pruebas:** el `pom.xml` incluye `spring-boot-starter-test`, pero aun no hay pruebas en `src/test`.
- `WebConfig` y `SecurityConfig` mapean `/js/**`, pero la carpeta `static/js` todavia no existe.
