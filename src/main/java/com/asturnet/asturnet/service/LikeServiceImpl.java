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
    private final PostService postService; // Necesitamos PostService para obtener el post por ID

    public LikeServiceImpl(LikeRepository likeRepository, PostService postService) {
        this.likeRepository = likeRepository;
        this.postService = postService;
    }

    @Override
    @Transactional
    public Like toggleLike(User user, Post post) {
        Optional<Like> existingLike = likeRepository.findByUserAndPost(user, post);

        if (existingLike.isPresent()) {
            // Si el like ya existe, lo eliminamos (quitar like)
            likeRepository.delete(existingLike.get());
            return null; // Indicamos que el like fue eliminado
        } else {
            // Si el like no existe, lo creamos (dar like)
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