package com.asturnet.asturnet.repository;

import com.asturnet.asturnet.model.Like;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Para encontrar si un like ya existe

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    // Encontrar un like específico dado un usuario y un post
    Optional<Like> findByUserAndPost(User user, Post post);

    // Contar el número de likes para un post
    long countByPost(Post post);

    // Verificar si un usuario ya le dio like a un post
    boolean existsByUserAndPost(User user, Post post);
}