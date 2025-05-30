package com.asturnet.asturnet.repository;

import com.asturnet.asturnet.model.User; // Importa tu entidad User
import org.springframework.data.jpa.repository.JpaRepository; // Importa JpaRepository
import org.springframework.stereotype.Repository; // Opcional, pero buena práctica para claridad

import java.util.Optional; // Importa Optional para manejar casos donde no se encuentra el usuario

@Repository // Anotación opcional pero recomendada para indicar que es un componente de persistencia
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository ya te da métodos CRUD básicos (save, findById, findAll, delete, etc.)

    // Método para buscar un usuario por su nombre de usuario.
    // Spring Data JPA generará la implementación automáticamente basándose en el nombre del método.
    Optional<User> findByUsername(String username);

    // Método para buscar un usuario por su email.
    Optional<User> findByEmail(String email);

    // Puedes añadir más métodos personalizados aquí si los necesitas, por ejemplo:
    // boolean existsByUsername(String username);
    // boolean existsByEmail(String email);
}