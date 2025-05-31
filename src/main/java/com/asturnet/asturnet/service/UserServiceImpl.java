package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.model.PrivacyLevel;
import com.asturnet.asturnet.repository.UserRepository; // Asegúrate de que esta importación exista
import com.asturnet.asturnet.dto.UserProfileUpdateRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional; // <-- ¡Importación necesaria para Optional!

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User registerNewUser(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya existe.");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("El email ya está registrado.");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setBio("");
        newUser.setFullName(""); // Inicializamos fullName
        newUser.setProfilePictureUrl(""); // Inicializamos profilePictureUrl

        newUser.setPrivacyLevel(PrivacyLevel.PUBLIC);
        newUser.setIsPrivate(false); // Es NOT NULL en la DB, inicializamos a false por defecto
        newUser.setIsBanned(false); // Por defecto, no baneado al registrar
        newUser.setRole("ROLE_USER"); // Por defecto, rol de usuario

        return userRepository.save(newUser);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado por nombre de usuario: " + username));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado por email: " + email));
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getIsPrivate() != null) {
            user.setIsPrivate(request.getIsPrivate());
            user.setPrivacyLevel(request.getIsPrivate() ? PrivacyLevel.PRIVATE : PrivacyLevel.PUBLIC);
        }

        return userRepository.save(user);
    }

    @Override
    public List<User> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(query, query);
    }

    // **NUEVOS MÉTODOS PARA EL BANEO AÑADIDOS AQUÍ**

    @Override
    @Transactional
    public void banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
        user.setIsBanned(true); // Establece el usuario como baneado
        userRepository.save(user); // Guarda los cambios
    }

    @Override
    @Transactional
    public void unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
        user.setIsBanned(false); // Desbanea al usuario
        userRepository.save(user);
    }
}