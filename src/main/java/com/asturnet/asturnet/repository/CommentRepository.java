package com.asturnet.asturnet.repository;

import com.asturnet.asturnet.model.Comment;
import com.asturnet.asturnet.model.Post; // Importamos Post para el método de búsqueda
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Encontrar todos los comentarios para una publicación específica, ordenados por fecha de creación ascendente
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);

    // Contar el número de comentarios para un post
    long countByPost(Post post);
}