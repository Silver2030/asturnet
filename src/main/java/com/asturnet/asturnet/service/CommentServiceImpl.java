package com.asturnet.asturnet.service;

import com.asturnet.asturnet.exception.ResourceNotFoundException; // Asegúrate de que esta excepción exista o usa RuntimeException
import com.asturnet.asturnet.model.Comment;
import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// IMPORTS NECESARIOS PARA VERIFICAR ROL DE ADMIN
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime; // Añadido para setCreatedAt/UpdatedAt si Comment.java los tiene
import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService; // Necesitamos PostService para obtener el post por ID si fuera necesario

    public CommentServiceImpl(CommentRepository commentRepository, PostService postService) {
        this.commentRepository = commentRepository;
        this.postService = postService; // Inyectamos PostService
    }

    // Método auxiliar para verificar si el usuario actual es admin
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
               authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Override
    @Transactional
    public Comment createComment(User user, Post post, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("El contenido del comentario no puede estar vacío.");
        }
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setContent(content);
        // Asumiendo que Comment.java tiene estos campos:
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> getCommentsByPost(Post post) {
        return commentRepository.findByPostOrderByCreatedAtAsc(post);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado con ID: " + commentId));

        // --- Lógica de autorización mejorada en el servicio ---
        // Un comentario puede ser eliminado por:
        // 1. Su propio autor
        // 2. El autor de la publicación a la que pertenece el comentario
        // 3. Un administrador
        if (!comment.getUser().getId().equals(currentUser.getId()) &&
            !comment.getPost().getUser().getId().equals(currentUser.getId()) &&
            !isAdmin()) { // <-- ¡Aquí se añade la condición para el administrador!
            throw new RuntimeException("No tienes permiso para eliminar este comentario.");
        }

        commentRepository.delete(comment);
    }

    // *** MÉTODO getCommentById CORREGIDO Y AHORA LANZA LA EXCEPCIÓN CORRECTA ***
    @Override // Asegúrate de que esta anotación esté si implementa una interfaz con este método
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado con ID: " + commentId));
    }
}