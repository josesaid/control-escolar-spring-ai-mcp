package com.escuela.controlescolar.config;

import com.escuela.controlescolar.model.Alumno;
import com.escuela.controlescolar.model.Aula;
import com.escuela.controlescolar.model.Calificacion;
import com.escuela.controlescolar.model.Grupo;
import com.escuela.controlescolar.model.Horario;
import com.escuela.controlescolar.model.Inscripcion;
import com.escuela.controlescolar.model.Maestro;
import com.escuela.controlescolar.model.Materia;
import com.escuela.controlescolar.model.Permiso;
import com.escuela.controlescolar.model.Rol;
import com.escuela.controlescolar.model.Usuario;
import com.escuela.controlescolar.repository.AlumnoRepository;
import com.escuela.controlescolar.repository.AulaRepository;
import com.escuela.controlescolar.repository.CalificacionRepository;
import com.escuela.controlescolar.repository.GrupoRepository;
import com.escuela.controlescolar.repository.HorarioRepository;
import com.escuela.controlescolar.repository.InscripcionRepository;
import com.escuela.controlescolar.repository.MaestroRepository;
import com.escuela.controlescolar.repository.MateriaRepository;
import com.escuela.controlescolar.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;
    private final AulaRepository aulaRepository;
    private final MaestroRepository maestroRepository;
    private final MateriaRepository materiaRepository;
    private final AlumnoRepository alumnoRepository;
    private final HorarioRepository horarioRepository;
    private final CalificacionRepository calificacionRepository;
    private final InscripcionRepository inscripcionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository,
                      GrupoRepository grupoRepository,
                      AulaRepository aulaRepository,
                      MaestroRepository maestroRepository,
                      MateriaRepository materiaRepository,
                      AlumnoRepository alumnoRepository,
                      HorarioRepository horarioRepository,
                      CalificacionRepository calificacionRepository,
                      InscripcionRepository inscripcionRepository,
                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.grupoRepository = grupoRepository;
        this.aulaRepository = aulaRepository;
        this.maestroRepository = maestroRepository;
        this.materiaRepository = materiaRepository;
        this.alumnoRepository = alumnoRepository;
        this.horarioRepository = horarioRepository;
        this.calificacionRepository = calificacionRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        sembrarUsuarios();
        List<Grupo> grupos = sembrarGrupos();
        List<Aula> aulas = sembrarAulas();
        List<Maestro> maestros = sembrarMaestros();
        List<Materia> materias = sembrarMaterias(maestros);
        List<Alumno> alumnos = sembrarAlumnos(grupos);
        sembrarHorarios(materias, maestros, aulas, grupos);
        sembrarCalificaciones(alumnos, materias);
        sembrarInscripciones(alumnos, grupos, materias);
    }

    private void sembrarUsuarios() {
        if (usuarioRepository.count() != 0) {
            return;
        }
        crearUsuario("admin", "admin123", "Administrador General", "admin@escuela.mx", Rol.ADMIN,
                EnumSet.allOf(Permiso.class));
        crearUsuario("director", "director123", "Director General", "director@escuela.mx", Rol.DIRECTOR,
                Set.of(Permiso.LEER_ALUMNOS, Permiso.LEER_CALIFICACIONES, Permiso.LEER_REPORTES,
                        Permiso.GESTION_USUARIOS, Permiso.GESTION_HORARIOS, Permiso.ASISTENTE_IA));
        crearUsuario("maestro", "maestro123", "Maestro Ejemplo", "maestro@escuela.mx", Rol.MAESTRO,
                Set.of(Permiso.LEER_ALUMNOS, Permiso.LEER_CALIFICACIONES, Permiso.EDITAR_CALIFICACIONES,
                        Permiso.ASISTENTE_IA));
        crearUsuario("secretaria", "secretaria123", "Secretaria General", "secretaria@escuela.mx", Rol.SECRETARIA,
                Set.of(Permiso.LEER_ALUMNOS, Permiso.CREAR_ALUMNOS, Permiso.EDITAR_ALUMNOS,
                        Permiso.LEER_CALIFICACIONES, Permiso.LEER_REPORTES, Permiso.GESTION_HORARIOS,
                        Permiso.ASISTENTE_IA));
        crearUsuario("alumno", "alumno123", "Alumno Ejemplo", "alumno@escuela.mx", Rol.ALUMNO,
                Set.of(Permiso.LEER_CALIFICACIONES, Permiso.ASISTENTE_IA));
    }

    private Usuario crearUsuario(String username, String password, String nombreCompleto, String email, Rol rol, Set<Permiso> permisos) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setEmail(email);
        usuario.setRol(rol);
        usuario.setPermisos(permisos);
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    private List<Grupo> sembrarGrupos() {
        if (grupoRepository.count() != 0) {
            return grupoRepository.findAll();
        }
        Grupo g1 = new Grupo();
        g1.setClave("1A");
        g1.setNombre("Primero A");
        g1.setGrado("1");
        g1.setTurno("Matutino");
        g1.setCapacidad(30);
        Grupo g2 = new Grupo();
        g2.setClave("2A");
        g2.setNombre("Segundo A");
        g2.setGrado("2");
        g2.setTurno("Matutino");
        g2.setCapacidad(30);
        Grupo g3 = new Grupo();
        g3.setClave("3A");
        g3.setNombre("Tercero A");
        g3.setGrado("3");
        g3.setTurno("Matutino");
        g3.setCapacidad(30);
        return grupoRepository.saveAll(List.of(g1, g2, g3));
    }

    private List<Aula> sembrarAulas() {
        if (aulaRepository.count() != 0) {
            return aulaRepository.findAll();
        }
        Aula a1 = new Aula();
        a1.setClave("A101");
        a1.setNombre("Aula 101");
        a1.setCapacidad(30);
        a1.setEdificio("A");
        a1.setTipo("TEORIA");
        Aula a2 = new Aula();
        a2.setClave("A102");
        a2.setNombre("Aula 102");
        a2.setCapacidad(30);
        a2.setEdificio("A");
        a2.setTipo("TEORIA");
        Aula a3 = new Aula();
        a3.setClave("LAB1");
        a3.setNombre("Laboratorio 1");
        a3.setCapacidad(25);
        a3.setEdificio("B");
        a3.setTipo("LABORATORIO");
        Aula a4 = new Aula();
        a4.setClave("B201");
        a4.setNombre("Aula 201");
        a4.setCapacidad(35);
        a4.setEdificio("B");
        a4.setTipo("TEORIA");
        return aulaRepository.saveAll(List.of(a1, a2, a3, a4));
    }

    private List<Maestro> sembrarMaestros() {
        if (maestroRepository.count() != 0) {
            return maestroRepository.findAll();
        }
        Maestro m1 = crearMaestro("EMP-001", "Juan", "Pérez", "García", "juan.perez@escuela.mx", "5551000001", "Matemáticas");
        Maestro m2 = crearMaestro("EMP-002", "María", "López", "Hernández", "maria.lopez@escuela.mx", "5551000002", "Español");
        Maestro m3 = crearMaestro("EMP-003", "Carlos", "Ramírez", "Torres", "carlos.ramirez@escuela.mx", "5551000003", "Ciencias");
        Maestro m4 = crearMaestro("EMP-004", "Ana", "González", "Ruiz", "ana.gonzalez@escuela.mx", "5551000004", "Historia e Inglés");
        Maestro m5 = crearMaestro("EMP-005", "Luis", "Martínez", "Sánchez", "luis.martinez@escuela.mx", "5551000005", "Informática");
        return List.of(m1, m2, m3, m4, m5);
    }

    private Maestro crearMaestro(String numeroEmpleado, String nombre, String apellidoPaterno, String apellidoMaterno, String email, String telefono, String especialidad) {
        Maestro maestro = new Maestro();
        maestro.setNumeroEmpleado(numeroEmpleado);
        maestro.setNombre(nombre);
        maestro.setApellidoPaterno(apellidoPaterno);
        maestro.setApellidoMaterno(apellidoMaterno);
        maestro.setEmail(email);
        maestro.setTelefono(telefono);
        maestro.setEspecialidad(especialidad);
        maestro.setActivo(true);
        return maestroRepository.save(maestro);
    }

    private List<Materia> sembrarMaterias(List<Maestro> maestros) {
        if (materiaRepository.count() != 0) {
            return materiaRepository.findAll();
        }
        Materia mat = crearMateria("MAT101", "Matemáticas", "Matemáticas básicas", 6, 5, maestros.get(0));
        Materia esp = crearMateria("ESP101", "Español", "Lengua española", 6, 5, maestros.get(1));
        Materia cie = crearMateria("CIE101", "Ciencias", "Ciencias naturales", 5, 4, maestros.get(2));
        Materia his = crearMateria("HIS101", "Historia", "Historia de México", 4, 3, maestros.get(3));
        Materia ing = crearMateria("ING101", "Inglés", "Inglés básico", 4, 3, maestros.get(3));
        Materia inf = crearMateria("INF101", "Informática", "Informática y computación", 5, 4, maestros.get(4));
        return List.of(mat, esp, cie, his, ing, inf);
    }

    private Materia crearMateria(String clave, String nombre, String descripcion, Integer creditos, Integer horasSemana, Maestro maestro) {
        Materia materia = new Materia();
        materia.setClave(clave);
        materia.setNombre(nombre);
        materia.setDescripcion(descripcion);
        materia.setCreditos(creditos);
        materia.setHorasSemana(horasSemana);
        materia.setMaestro(maestro);
        return materiaRepository.save(materia);
    }

    private List<Alumno> sembrarAlumnos(List<Grupo> grupos) {
        if (alumnoRepository.count() != 0) {
            return alumnoRepository.findAll();
        }
        Alumno a1 = crearAlumno("ESC-001", "Sofía", "García", "Mendoza", "sofia.garcia@escuela.mx", "5552000001", LocalDate.of(2012, 3, 10), "Femenino", grupos.get(0));
        Alumno a2 = crearAlumno("ESC-002", "Diego", "Hernández", "López", "diego.hernandez@escuela.mx", "5552000002", LocalDate.of(2012, 5, 14), "Masculino", grupos.get(0));
        Alumno a3 = crearAlumno("ESC-003", "Valeria", "Torres", "Reyes", "valeria.torres@escuela.mx", "5552000003", LocalDate.of(2012, 8, 22), "Femenino", grupos.get(0));
        Alumno a4 = crearAlumno("ESC-004", "Mateo", "Ruiz", "Castro", "mateo.ruiz@escuela.mx", "5552000004", LocalDate.of(2011, 2, 5), "Masculino", grupos.get(1));
        Alumno a5 = crearAlumno("ESC-005", "Camila", "Morales", "Vega", "camila.morales@escuela.mx", "5552000005", LocalDate.of(2011, 6, 18), "Femenino", grupos.get(1));
        Alumno a6 = crearAlumno("ESC-006", "Sebastián", "Ortiz", "Navarro", "sebastian.ortiz@escuela.mx", "5552000006", LocalDate.of(2011, 9, 30), "Masculino", grupos.get(1));
        Alumno a7 = crearAlumno("ESC-007", "Isabella", "Soto", "Paredes", "isabella.soto@escuela.mx", "5552000007", LocalDate.of(2010, 4, 12), "Femenino", grupos.get(2));
        Alumno a8 = crearAlumno("ESC-008", "Emiliano", "Rojas", "Fuentes", "emiliano.rojas@escuela.mx", "5552000008", LocalDate.of(2010, 11, 25), "Masculino", grupos.get(2));
        return List.of(a1, a2, a3, a4, a5, a6, a7, a8);
    }

    private Alumno crearAlumno(String matricula, String nombre, String apellidoPaterno, String apellidoMaterno, String email, String telefono, LocalDate fechaNacimiento, String genero, Grupo grupo) {
        Alumno alumno = new Alumno();
        alumno.setMatricula(matricula);
        alumno.setNombre(nombre);
        alumno.setApellidoPaterno(apellidoPaterno);
        alumno.setApellidoMaterno(apellidoMaterno);
        alumno.setEmail(email);
        alumno.setTelefono(telefono);
        alumno.setFechaNacimiento(fechaNacimiento);
        alumno.setGenero(genero);
        alumno.setGrupo(grupo);
        alumno.setActivo(true);
        return alumnoRepository.save(alumno);
    }

    private void sembrarHorarios(List<Materia> materias, List<Maestro> maestros, List<Aula> aulas, List<Grupo> grupos) {
        if (horarioRepository.count() != 0) {
            return;
        }
        crearHorario(materias.get(0), maestros.get(0), aulas.get(0), grupos.get(0), "LUNES", LocalTime.of(8, 0), LocalTime.of(9, 0));
        crearHorario(materias.get(1), maestros.get(1), aulas.get(0), grupos.get(0), "MARTES", LocalTime.of(8, 0), LocalTime.of(9, 0));
        crearHorario(materias.get(2), maestros.get(2), aulas.get(2), grupos.get(1), "MIERCOLES", LocalTime.of(9, 0), LocalTime.of(10, 0));
        crearHorario(materias.get(3), maestros.get(3), aulas.get(1), grupos.get(1), "JUEVES", LocalTime.of(10, 0), LocalTime.of(11, 0));
        crearHorario(materias.get(4), maestros.get(3), aulas.get(1), grupos.get(2), "VIERNES", LocalTime.of(8, 0), LocalTime.of(9, 0));
        crearHorario(materias.get(5), maestros.get(4), aulas.get(2), grupos.get(2), "LUNES", LocalTime.of(11, 0), LocalTime.of(12, 0));
        crearHorario(materias.get(0), maestros.get(0), aulas.get(3), grupos.get(1), "VIERNES", LocalTime.of(9, 0), LocalTime.of(10, 0));
    }

    private void crearHorario(Materia materia, Maestro maestro, Aula aula, Grupo grupo, String diaSemana, LocalTime horaInicio, LocalTime horaFin) {
        Horario horario = new Horario();
        horario.setMateria(materia);
        horario.setMaestro(maestro);
        horario.setAula(aula);
        horario.setGrupo(grupo);
        horario.setDiaSemana(diaSemana);
        horario.setHoraInicio(horaInicio);
        horario.setHoraFin(horaFin);
        horarioRepository.save(horario);
    }

    private void sembrarCalificaciones(List<Alumno> alumnos, List<Materia> materias) {
        if (calificacionRepository.count() != 0) {
            return;
        }
        double[] valores = {9.5, 8.0, 7.5, 10.0, 6.5, 9.0, 8.5, 7.0, 9.8, 8.2, 6.0, 9.1, 7.8, 8.8, 5.5, 9.3, 7.2, 8.6, 9.0, 6.8, 8.4, 9.6, 7.9, 8.1};
        String[] tipos = {"PARCIAL", "FINAL"};
        int i = 0;
        for (Alumno alumno : alumnos) {
            for (int j = 0; j < 3; j++) {
                Materia materia = materias.get((alumnos.indexOf(alumno) + j) % materias.size());
                Calificacion calificacion = new Calificacion();
                calificacion.setAlumno(alumno);
                calificacion.setMateria(materia);
                calificacion.setPeriodo("2026-1");
                calificacion.setValor(valores[i % valores.length]);
                calificacion.setTipoEvaluacion(tipos[i % tipos.length]);
                calificacion.setFecha(LocalDate.of(2026, 3, 15));
                calificacion.setObservaciones("Calificación de ejemplo");
                calificacionRepository.save(calificacion);
                i++;
            }
        }
    }

    private void sembrarInscripciones(List<Alumno> alumnos, List<Grupo> grupos, List<Materia> materias) {
        if (inscripcionRepository.count() != 0) {
            return;
        }
        for (Alumno alumno : alumnos) {
            for (int j = 0; j < 2; j++) {
                Inscripcion inscripcion = new Inscripcion();
                inscripcion.setAlumno(alumno);
                inscripcion.setGrupo(alumno.getGrupo());
                inscripcion.setMateria(materias.get((alumnos.indexOf(alumno) + j) % materias.size()));
                inscripcion.setPeriodo("2026-1");
                inscripcion.setFechaInscripcion(LocalDate.of(2026, 1, 10));
                inscripcionRepository.save(inscripcion);
            }
        }
    }
}
