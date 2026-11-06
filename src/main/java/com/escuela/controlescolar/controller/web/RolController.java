package com.escuela.controlescolar.controller.web;

import com.escuela.controlescolar.service.UsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador web MVC para consultar los roles y permisos del sistema.
 * Solo ADMIN y DIRECTOR (igual que la gestión de usuarios).
 */
@Controller
@RequestMapping("/roles")
@PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
public class RolController {

    private final UsuarioService usuarioService;

    public RolController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("")
    public String listar(Model model) {
        model.addAttribute("roles", usuarioService.listarRoles());
        model.addAttribute("permisos", usuarioService.listarPermisos());
        return "roles/list";
    }
}
