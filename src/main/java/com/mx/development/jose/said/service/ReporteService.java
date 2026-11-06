package com.mx.development.jose.said.service;

import com.mx.development.jose.said.model.Alumno;
import com.mx.development.jose.said.model.Calificacion;
import com.mx.development.jose.said.model.Grupo;
import com.mx.development.jose.said.model.Materia;
import com.mx.development.jose.said.repository.AlumnoRepository;
import com.mx.development.jose.said.repository.CalificacionRepository;
import com.mx.development.jose.said.repository.GrupoRepository;
import com.mx.development.jose.said.repository.MaestroRepository;
import com.mx.development.jose.said.repository.MateriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReporteService {

    private final AlumnoRepository alumnoRepository;
    private final MaestroRepository maestroRepository;
    private final MateriaRepository materiaRepository;
    private final GrupoRepository grupoRepository;
    private final CalificacionRepository calificacionRepository;
    private final AlumnoService alumnoService;
    private final CalificacionService calificacionService;

    public ReporteService(AlumnoRepository alumnoRepository,
                          MaestroRepository maestroRepository,
                          MateriaRepository materiaRepository,
                          GrupoRepository grupoRepository,
                          CalificacionRepository calificacionRepository,
                          AlumnoService alumnoService,
                          CalificacionService calificacionService) {
        this.alumnoRepository = alumnoRepository;
        this.maestroRepository = maestroRepository;
        this.materiaRepository = materiaRepository;
        this.grupoRepository = grupoRepository;
        this.calificacionRepository = calificacionRepository;
        this.alumnoService = alumnoService;
        this.calificacionService = calificacionService;
    }

    public long totalAlumnos() {
        return alumnoRepository.count();
    }

    public long totalMaestros() {
        return maestroRepository.count();
    }

    public long totalMaterias() {
        return materiaRepository.count();
    }

    public long totalGrupos() {
        return grupoRepository.count();
    }

    public Double promedioGeneral() {
        return calificacionService.promedioGeneral();
    }

    public Map<String, Object> resumenGeneral() {
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("totalAlumnos", totalAlumnos());
        resumen.put("totalMaestros", totalMaestros());
        resumen.put("totalMaterias", totalMaterias());
        resumen.put("totalGrupos", totalGrupos());
        resumen.put("promedioGeneral", promedioGeneral());
        resumen.put("totalCalificaciones", calificacionRepository.count());
        return resumen;
    }

    public List<Map<String, Object>> promedioPorGrupo() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        List<Grupo> grupos = grupoRepository.findAll();
        for (Grupo grupo : grupos) {
            if (grupo == null) {
                continue;
            }
            List<Alumno> alumnos = alumnoRepository.findByGrupoId(grupo.getId());
            int totalAlumnos = alumnos == null ? 0 : alumnos.size();
            double suma = 0.0;
            int total = 0;
            if (alumnos != null) {
                for (Alumno alumno : alumnos) {
                    if (alumno == null) {
                        continue;
                    }
                    List<Calificacion> calificaciones =
                            calificacionRepository.findByAlumnoId(alumno.getId());
                    if (calificaciones != null) {
                        for (Calificacion calificacion : calificaciones) {
                            if (calificacion != null && calificacion.getValor() != null) {
                                suma += calificacion.getValor();
                                total++;
                            }
                        }
                    }
                }
            }
            double promedio = total == 0 ? 0.0 : suma / total;
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("grupo", grupo.getNombre());
            fila.put("clave", grupo.getClave());
            fila.put("totalAlumnos", totalAlumnos);
            fila.put("promedio", promedio);
            resultado.add(fila);
        }
        return resultado;
    }

    public List<Map<String, Object>> promedioPorMateria() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        List<Materia> materias = materiaRepository.findAll();
        for (Materia materia : materias) {
            if (materia == null) {
                continue;
            }
            List<Calificacion> calificaciones =
                    calificacionRepository.findByMateriaId(materia.getId());
            int total = calificaciones == null ? 0 : calificaciones.size();
            Double promedio = calificacionService.promedioPorMateria(materia.getId());
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("materia", materia.getNombre());
            fila.put("clave", materia.getClave());
            fila.put("promedio", promedio == null ? 0.0 : promedio);
            fila.put("totalCalificaciones", total);
            resultado.add(fila);
        }
        return resultado;
    }

    public List<Map<String, Object>> topAlumnos(int limite) {
        List<Map<String, Object>> conPromedio = new ArrayList<>();
        List<Alumno> alumnos = alumnoService.listarTodos();
        for (Alumno alumno : alumnos) {
            if (alumno == null) {
                continue;
            }
            Double promedio = calificacionService.promedioPorAlumno(alumno.getId());
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("matricula", alumno.getMatricula());
            fila.put("nombre", alumno.getNombreCompleto());
            fila.put("promedio", promedio == null ? 0.0 : promedio);
            conPromedio.add(fila);
        }
        conPromedio.sort(Comparator.comparingDouble(
                (Map<String, Object> fila) -> ((Number) fila.get("promedio")).doubleValue()
        ).reversed());
        if (limite <= 0) {
            return List.of();
        }
        if (limite >= conPromedio.size()) {
            return conPromedio;
        }
        return new ArrayList<>(conPromedio.subList(0, limite));
    }

    public List<Map<String, Object>> alumnosEnRiesgo() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        List<Alumno> alumnos = alumnoService.listarTodos();
        for (Alumno alumno : alumnos) {
            if (alumno == null) {
                continue;
            }
            Double promedio = calificacionService.promedioPorAlumno(alumno.getId());
            double valor = promedio == null ? 0.0 : promedio;
            if (valor < 6.0) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("matricula", alumno.getMatricula());
                fila.put("nombre", alumno.getNombreCompleto());
                fila.put("promedio", valor);
                resultado.add(fila);
            }
        }
        return resultado;
    }

    public List<Map<String, Object>> reprobadosPorMateria() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        List<Materia> materias = materiaRepository.findAll();
        for (Materia materia : materias) {
            if (materia == null) {
                continue;
            }
            List<Calificacion> calificaciones =
                    calificacionRepository.findByMateriaId(materia.getId());
            int total = calificaciones == null ? 0 : calificaciones.size();
            long reprobados = 0;
            if (calificaciones != null) {
                reprobados = calificaciones.stream()
                        .filter(c -> c != null && c.getValor() != null && c.getValor() < 6.0)
                        .count();
            }
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("materia", materia.getNombre());
            fila.put("reprobados", reprobados);
            fila.put("total", total);
            resultado.add(fila);
        }
        return resultado;
    }

    public String generarCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("Matricula,Alumno,Grupo,Materia,Periodo,Calificacion\n");
        List<Calificacion> calificaciones = calificacionRepository.findAll();
        if (calificaciones != null) {
            for (Calificacion calificacion : calificaciones) {
                if (calificacion == null) {
                    continue;
                }
                Alumno alumno = calificacion.getAlumno();
                Materia materia = calificacion.getMateria();
                String matricula = alumno != null && alumno.getMatricula() != null
                        ? alumno.getMatricula() : "";
                String nombre = alumno != null ? alumno.getNombreCompleto() : "";
                String grupo = "";
                if (alumno != null && alumno.getGrupo() != null
                        && alumno.getGrupo().getClave() != null) {
                    grupo = alumno.getGrupo().getClave();
                }
                String nombreMateria = materia != null && materia.getNombre() != null
                        ? materia.getNombre() : "";
                String periodo = calificacion.getPeriodo() != null
                        ? calificacion.getPeriodo() : "";
                String valor = calificacion.getValor() != null
                        ? String.valueOf(calificacion.getValor()) : "";
                csv.append(escaparCsv(matricula)).append(",")
                        .append(escaparCsv(nombre)).append(",")
                        .append(escaparCsv(grupo)).append(",")
                        .append(escaparCsv(nombreMateria)).append(",")
                        .append(escaparCsv(periodo)).append(",")
                        .append(escaparCsv(valor)).append("\n");
            }
        }
        return csv.toString();
    }

    private String escaparCsv(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
