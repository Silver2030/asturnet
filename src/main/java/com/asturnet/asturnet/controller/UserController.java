package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Listar todos los usuarios
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Crear un usuario nuevo
    @PostMapping
    public User createUser(@RequestBody User user) {
        // Aquí deberías agregar lógica para hashear la contraseña y validar
        return userRepository.save(user);
    }
}
