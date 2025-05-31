package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.Comment;
import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService; // Necesitamos PostService para obtener el post por ID si fuera necesario

    public CommentServiceImpl(CommentRepository commentRepository, PostService postService) {
        this.commentRepository = commentRepository;
        this.postService = postService; // Inyectamos PostService
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

        // Un comentario puede ser eliminado por:
        // 1. Su propio autor
        // 2. El autor de la publicación a la que pertenece el comentario
        if (!comment.getUser().getId().equals(currentUser.getId()) &&
            !comment.getPost().getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar este comentario.");
        }

        commentRepository.delete(comment);
    }
}