package com.asturnet.asturnet.repository;

import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User; // Necesitamos User para el método findByUser
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository // Indica que esta interfaz es un componente de repositorio de Spring
public interface PostRepository extends JpaRepository<Post, Long> {
    // Método para encontrar posts de un usuario específico, ordenados por fecha de creación descendente
    List<Post> findByUserOrderByCreatedAtDesc(User user);

    // Método para encontrar todos los posts, ordenados por fecha de creación descendente (para el feed general)
    List<Post> findByOrderByCreatedAtDesc();

    List<Post> findAllByOrderByCreatedAtDesc();
}