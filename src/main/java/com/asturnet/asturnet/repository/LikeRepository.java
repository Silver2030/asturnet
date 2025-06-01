package com.asturnet.asturnet.repository;

import com.asturnet.asturnet.model.Like;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserAndPost(User user, Post post);

    long countByPost(Post post);

    boolean existsByUserAndPost(User user, Post post);
}