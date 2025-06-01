package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import java.util.List;

public interface PostService {
    Post createPost(User user, String content, String imageUrl, String videoUrl);
    Post getPostById(Long postId);
    List<Post> getAllPosts();
    List<Post> getPostsByUser(User user);
    void deletePost(Long postId, User currentUser);

    List<Post> getAllPostsWithUserAndComments();

    List<Post> getHomeFeedPosts(User currentUser);
}