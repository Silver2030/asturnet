package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.User; // Tu entidad User
import com.asturnet.asturnet.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Determina el estado 'enabled' para Spring Security
        // Será 'true' si el usuario NO está baneado (isBanned es false)
        // Será 'false' si el usuario SÍ está baneado (isBanned es true)
        boolean accountEnabled = !user.getIsBanned(); // O user.isBanned() si Lombok lo genera así

        logger.info("Cargando usuario: {} con rol: {}. ¿Baneado?: {}. ¿Habilitado para login?: {}",
                    user.getUsername(), user.getRole(), user.getIsBanned(), accountEnabled);


        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),             // 1. Nombre de usuario
                user.getPassword(),             // 2. Contraseña (ya hasheada)
                accountEnabled,                 // 3. enabled (¡AQUÍ ESTÁ EL CAMBIO CLAVE!)
                true,                           // 4. accountNonExpired
                true,                           // 5. credentialsNonExpired
                true,                           // 6. accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole())) // 7. Colección de GrantedAuthorities
        );
    }
}