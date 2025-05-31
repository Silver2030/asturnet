package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.Like; // Importamos la entidad Like

public interface LikeService {
    Like toggleLike(User user, Post post); // Dar like o quitar like
    boolean isLikedByUser(User user, Post post); // Verificar si un usuario ya le dio like
    long getLikesCountForPost(Post post); // Obtener el número de likes para un post
}