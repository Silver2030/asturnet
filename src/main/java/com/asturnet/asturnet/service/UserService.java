package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.dto.UserProfileUpdateRequest; // Importa el nuevo DTO

public interface UserService {
    User registerNewUser(String username, String email, String password);
    User findByUsername(String username);
    User findByEmail(String email);
    User updateProfile(Long userId, UserProfileUpdateRequest request); // Nuevo método
    // Puedes añadir más métodos según las necesidades:
    // void deleteUser(Long userId);
}