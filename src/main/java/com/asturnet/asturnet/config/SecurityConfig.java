package com.asturnet.asturnet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Importa BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain; // Importa SecurityFilterChain

@Configuration // Indica que esta clase contiene definiciones de beans y configuración de Spring
@EnableWebSecurity // Habilita la configuración de seguridad web de Spring Security
public class SecurityConfig {

    @Bean // Define un bean de Spring para el PasswordEncoder
    public PasswordEncoder passwordEncoder() {
        // Usamos BCryptPasswordEncoder, que es el algoritmo recomendado para hashear contraseñas
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**").permitAll() // Permite acceso público a estas rutas
                .anyRequest().authenticated() // Cualquier otra solicitud requiere autenticación
            )
            .formLogin(form -> form
                .loginPage("/login") // Especifica la página de login personalizada
                .permitAll() // Permite acceso a la página de login para todos
            )
            .logout(logout -> logout
                .permitAll() // Permite que todos accedan al logout
            );
            // Si tu aplicación es un backend puro (sin CSRF para APIs), podrías deshabilitar CSRF:
            // .csrf(csrf -> csrf.disable());
            // Pero como es full-stack y habrá formularios, es mejor dejar CSRF habilitado por defecto.

        return http.build();
    }
}