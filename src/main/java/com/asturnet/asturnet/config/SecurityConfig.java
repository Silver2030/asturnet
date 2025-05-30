package com.asturnet.asturnet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder; // Importa AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.asturnet.asturnet.service.UserDetailsServiceImpl; // ¡Importa tu UserDetailsServiceImpl!

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService; // Inyecta tu UserDetailsService

    // Constructor para inyectar UserDetailsServiceImpl
    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean para configurar el AuthenticationManager
    // Esto es necesario para que Spring Security sepa cómo autenticar a los usuarios
    // usando nuestro UserDetailsService y PasswordEncoder.
    // NOTA: Para Spring Boot 2.7+ y Spring Security 5.7+, la configuración
    // del AuthenticationManagerBuilder ha cambiado un poco.
    // Usaremos un enfoque más nuevo con DaoAuthenticationProvider directamente en el FilterChain.
    // Por ahora, solo necesitamos el passwordEncoder y UserDetailsService disponibles.


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true) // Redirige a /home (o a la página de inicio) después del login exitoso
                .failureUrl("/login?error") // Redirige a /login?error si falla el login
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // URL para cerrar sesión
                .logoutSuccessUrl("/login?logout") // Redirige a /login?logout después de cerrar sesión
                .permitAll()
            );

        return http.build();
    }

    // En Spring Security 6 (que usas con Spring Boot 3), no se necesita un AuthenticationManagerBuilder explícito
    // como bean. La configuración del DaoAuthenticationProvider se hace internamente
    // si el UserDetailsService y el PasswordEncoder están disponibles como beans.

    // SIN EMBARGO, para que la autenticación funcione correctamente,
    // necesitamos decirle a Spring Security que use nuestro UserDetailsService para la autenticación
    // Esto se hace a través de un AuthenticationProvider.

    // AÑADE ESTE BEAN PARA CONFIGURAR EL PROVEEDOR DE AUTENTICACIÓN
    @Bean
    public org.springframework.security.authentication.dao.DaoAuthenticationProvider authenticationProvider() {
        org.springframework.security.authentication.dao.DaoAuthenticationProvider authProvider = new org.springframework.security.authentication.dao.DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Establece nuestro UserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder()); // Establece nuestro PasswordEncoder
        return authProvider;
    }
}