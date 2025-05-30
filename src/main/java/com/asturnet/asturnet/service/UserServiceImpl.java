package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.model.PrivacyLevel;
import com.asturnet.asturnet.repository.UserRepository; // Importa tu repositorio
import org.springframework.security.crypto.password.PasswordEncoder; // Importa PasswordEncoder
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Para manejar transacciones

@Service // Indica que esta clase es un componente de servicio de Spring
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Inyectamos PasswordEncoder

    // Inyección de dependencias a través del constructor
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional // Indica que este método debe ejecutarse dentro de una transacción de base de datos
    public User registerNewUser(String username, String email, String password) {
        // Validación básica (puedes expandirla más adelante)
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya existe."); // O una excepción personalizada
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("El email ya está registrado."); // O una excepción personalizada
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        // ¡Importante! Codifica la contraseña antes de guardarla en la base de datos
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setBio(""); // Valores por defecto
        newUser.setPhotoUrl(""); // Valores por defecto
        newUser.setPrivacyLevel(PrivacyLevel.PUBLIC); // Valor por defecto

        return userRepository.save(newUser); // Guarda el nuevo usuario en la BD
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado por nombre: " + username));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado por email: " + email));
    }
}