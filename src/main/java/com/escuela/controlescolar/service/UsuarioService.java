package com.escuela.controlescolar.service;

import com.escuela.controlescolar.model.Permiso;
import com.escuela.controlescolar.model.Rol;
import com.escuela.controlescolar.model.Usuario;
import com.escuela.controlescolar.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.orElse(null);
    }

    public Usuario buscarPorUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        Optional<Usuario> usuario = usuarioRepository.findByUsername(username);
        return usuario.orElse(null);
    }

    @Transactional
    public Usuario guardar(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            // Al editar, contrasena en blanco = conservar la anterior.
            if (usuario.getId() != null) {
                Usuario existente = usuarioRepository.findById(usuario.getId()).orElse(null);
                if (existente != null) {
                    usuario.setPassword(existente.getPassword());
                }
            } else {
                usuario.setPassword(passwordEncoder.encode("cambiar123"));
            }
        } else if (!usuario.getPassword().startsWith("$2a$")) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            return;
        }
        usuarioRepository.deleteById(id);
    }

    public long contar() {
        return usuarioRepository.count();
    }

    public List<Rol> listarRoles() {
        return Arrays.asList(Rol.values());
    }

    public List<Permiso> listarPermisos() {
        return Arrays.asList(Permiso.values());
    }
}
