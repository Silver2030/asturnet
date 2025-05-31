package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.User; // Asegúrate de que esta importación esté presente
import com.asturnet.asturnet.dto.UserProfileUpdateRequest; // Importa el nuevo DTO

import java.util.List;
import java.util.Optional; // Necesario para Optional<User>

public interface UserService {
    User registerNewUser(String username, String email, String password);
    User findByUsername(String username); // Retorna User (o null si no encontrado)
    User findByEmail(String email);

    // Métodos que faltaban y son necesarios para ProfileController:
    Optional<User> findById(Long id); // Retorna Optional<User>
    User updateUser(User user);       // Para guardar un User modificado

    // El método updateProfile que ya tenías:
    User updateProfile(Long userId, UserProfileUpdateRequest request);

    // Puedes añadir más métodos según las necesidades:
    // void deleteUser(Long userId);
    List<User> searchUsers(String query);

    // **NUEVOS MÉTODOS PARA EL BANEO**
    void banUser(Long userId);
    void unbanUser(Long userId);
}