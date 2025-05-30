package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.model.PrivacyLevel;
import com.asturnet.asturnet.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // Aseguramos que se establezcan los nuevos campos que tu BD espera
        newUser.setFullName(""); // Inicializamos fullName
        newUser.setProfilePictureUrl(""); // Inicializamos profilePictureUrl

        newUser.setPrivacyLevel(PrivacyLevel.PUBLIC);
        newUser.setIsPrivate(false); // Es NOT NULL en la DB, inicializamos a false por defecto

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
}