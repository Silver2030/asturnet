package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.Comment;
import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import java.util.List;

public interface CommentService {
    Comment createComment(User user, Post post, String content);
    List<Comment> getCommentsByPost(Post post);
    void deleteComment(Long commentId, User currentUser); // Eliminar un comentario (solo el autor o si eres el dueño del post)
}