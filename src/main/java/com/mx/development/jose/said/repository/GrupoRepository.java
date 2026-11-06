package com.mx.development.jose.said.repository;

import com.mx.development.jose.said.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    Optional<Grupo> findByClave(String clave);
}
