package com.escuela.controlescolar.config;

import com.escuela.controlescolar.model.Alumno;
import com.escuela.controlescolar.model.Aula;
import com.escuela.controlescolar.model.Grupo;
import com.escuela.controlescolar.model.Maestro;
import com.escuela.controlescolar.model.Materia;
import com.escuela.controlescolar.repository.AlumnoRepository;
import com.escuela.controlescolar.repository.AulaRepository;
import com.escuela.controlescolar.repository.GrupoRepository;
import com.escuela.controlescolar.repository.MaestroRepository;
import com.escuela.controlescolar.repository.MateriaRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Convierte el id (String) que envian los <select> de los formularios
 * Thymeleaf (th:field="*{grupo}", *{maestro}, etc.) en la entidad JPA
 * correspondiente. Sin esto, el binding de @ModelAttribute fallaria.
 */
@Configuration
public class EntityConverters implements WebMvcConfigurer {

    private final GrupoRepository grupoRepository;
    private final MaestroRepository maestroRepository;
    private final MateriaRepository materiaRepository;
    private final AulaRepository aulaRepository;
    private final AlumnoRepository alumnoRepository;

    public EntityConverters(GrupoRepository grupoRepository,
                            MaestroRepository maestroRepository,
                            MateriaRepository materiaRepository,
                            AulaRepository aulaRepository,
                            AlumnoRepository alumnoRepository) {
        this.grupoRepository = grupoRepository;
        this.maestroRepository = maestroRepository;
        this.materiaRepository = materiaRepository;
        this.aulaRepository = aulaRepository;
        this.alumnoRepository = alumnoRepository;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, Grupo>() {
            @Override public Grupo convert(String id) {
                if (id == null || id.isBlank()) return null;
                return grupoRepository.findById(Long.valueOf(id)).orElse(null);
            }
        });
        registry.addConverter(new Converter<String, Maestro>() {
            @Override public Maestro convert(String id) {
                if (id == null || id.isBlank()) return null;
                return maestroRepository.findById(Long.valueOf(id)).orElse(null);
            }
        });
        registry.addConverter(new Converter<String, Materia>() {
            @Override public Materia convert(String id) {
                if (id == null || id.isBlank()) return null;
                return materiaRepository.findById(Long.valueOf(id)).orElse(null);
            }
        });
        registry.addConverter(new Converter<String, Aula>() {
            @Override public Aula convert(String id) {
                if (id == null || id.isBlank()) return null;
                return aulaRepository.findById(Long.valueOf(id)).orElse(null);
            }
        });
        registry.addConverter(new Converter<String, Alumno>() {
            @Override public Alumno convert(String id) {
                if (id == null || id.isBlank()) return null;
                return alumnoRepository.findById(Long.valueOf(id)).orElse(null);
            }
        });
    }
}
