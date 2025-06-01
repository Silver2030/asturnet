package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.Like;
import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.repository.LikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;

    public LikeServiceImpl(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    @Override
    @Transactional
    public Like toggleLike(User user, Post post) {
        Optional<Like> existingLike = likeRepository.findByUserAndPost(user, post);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            return null; 
        } else {
            Like newLike = new Like();
            newLike.setUser(user);
            newLike.setPost(post);
            return likeRepository.save(newLike);
        }
    }

    @Override
    public boolean isLikedByUser(User user, Post post) {
        return likeRepository.existsByUserAndPost(user, post);
    }

    @Override
    public long getLikesCountForPost(Post post) {
        return likeRepository.countByPost(post);
    }
}