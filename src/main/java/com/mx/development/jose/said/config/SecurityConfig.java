package com.mx.development.jose.said.config;

import com.mx.development.jose.said.model.Usuario;
import com.mx.development.jose.said.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return username -> {
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (usuario.getRol() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
            }
            if (usuario.getPermisos() != null) {
                usuario.getPermisos().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.name())));
            }
            return new User(usuario.getUsername(), usuario.getPassword(), usuario.isActivo(),
                    true, true, true, authorities);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/mcp", "/mcp/**", "/h2-console/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/h2-console/**").permitAll()
                        // /mcp y /mcp/** permitAll: el MCP se protege en producción
                        // (autenticación por token / red interna), documentado aquí.
                        .requestMatchers("/mcp", "/mcp/**").permitAll()
                        .requestMatchers("/usuarios/**", "/roles").hasAnyRole("ADMIN", "DIRECTOR")
                        .requestMatchers("/calificaciones/**").hasAnyRole("ADMIN", "DIRECTOR", "MAESTRO", "SECRETARIA")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
