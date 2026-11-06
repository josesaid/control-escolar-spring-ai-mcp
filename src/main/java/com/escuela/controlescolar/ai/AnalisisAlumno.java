package com.escuela.controlescolar.ai;

import java.util.List;

/**
 * Análisis académico de un alumno, generado por el modelo como
 * <em>structured output</em> ({@code ChatClient...entity(AnalisisAlumno.class)}).
 *
 * @param nivelRiesgo BAJO, MEDIO o ALTO
 */
public record AnalisisAlumno(
        String matricula,
        String nombre,
        Double promedio,
        Double porcentajeAsistencia,
        String nivelRiesgo,
        List<String> fortalezas,
        List<String> areasDeOportunidad,
        List<String> recomendaciones) {
}
