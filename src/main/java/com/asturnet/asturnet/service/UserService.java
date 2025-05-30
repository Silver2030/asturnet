package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.model.PrivacyLevel; // Importar PrivacyLevel

public interface UserService {
    User registerNewUser(String username, String email, String password);
    User findByUsername(String username);
    User findByEmail(String email);
    // Puedes añadir más métodos según las necesidades:
    // User updateProfile(Long userId, String bio, String photoUrl, PrivacyLevel privacyLevel);
    // void deleteUser(Long userId);
}