package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.Like; 

public interface LikeService {
    Like toggleLike(User user, Post post); 
    boolean isLikedByUser(User user, Post post); 
    long getLikesCountForPost(Post post); 
}