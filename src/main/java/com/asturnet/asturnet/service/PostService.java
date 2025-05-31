package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import java.util.List;
import java.util.Optional; // Puede que lo necesites si usas Optional en algún método, aunque para este no.

public interface PostService {
    Post createPost(User user, String content, String imageUrl, String videoUrl);
    Post getPostById(Long postId);
    List<Post> getAllPosts(); // Para obtener todas las publicaciones (feed general)
    List<Post> getPostsByUser(User user); // Usa este método
    void deletePost(Long postId, User currentUser);

    // *** ¡AÑADE ESTA LÍNEA! ***
    List<Post> findAllPostsOrderedByCreatedAtDesc();
}