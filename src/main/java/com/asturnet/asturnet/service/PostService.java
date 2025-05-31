package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User; // Importamos User para el método createPost
import java.util.List;

public interface PostService {
    Post createPost(User user, String content, String imageUrl, String videoUrl);
    Post getPostById(Long postId);
    List<Post> getAllPosts(); // Para obtener todas las publicaciones (feed general)
    List<Post> getPostsByUser(User user); // Para obtener las publicaciones de un usuario específico
    void deletePost(Long postId, User currentUser); // Eliminar un post (solo el autor o admin)
    // Puedes añadir más métodos como updatePost si la edición de posts es una característica.
}