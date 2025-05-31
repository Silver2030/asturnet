package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.repository.PostRepository; // Importa el repositorio de Posts
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Para manejar transacciones

import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository; // Inyecta el repositorio de Posts

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    @Transactional // Esta operación debe ser transaccional
    public Post createPost(User user, String content, String imageUrl, String videoUrl) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("El contenido de la publicación no puede estar vacío.");
        }
        Post post = new Post();
        post.setUser(user); // Asigna el usuario autor
        post.setContent(content);
        post.setImageUrl(imageUrl);
        post.setVideoUrl(videoUrl);
        return postRepository.save(post); // Guarda el post en la base de datos
    }

    @Override
    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada con ID: " + postId));
    }

    @Override
    public List<Post> getAllPosts() {
        return postRepository.findByOrderByCreatedAtDesc(); // Usa el método que creamos en el repositorio
    }

    @Override
    public List<Post> getPostsByUser(User user) {
        return postRepository.findByUserOrderByCreatedAtDesc(user); // Usa el método que creamos
    }

    @Override
    @Transactional // Esta operación debe ser transaccional
    public void deletePost(Long postId, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada con ID: " + postId));

        // Solo el autor del post puede borrarlo
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar esta publicación.");
        }

        postRepository.delete(post); // Elimina el post
    }

        // *** ¡AÑADE ESTA IMPLEMENTACIÓN! ***
    @Override
    public List<Post> findAllPostsOrderedByCreatedAtDesc() {
        return postRepository.findAllByOrderByCreatedAtDesc(); // Delega al repositorio para la ordenación
    }
}