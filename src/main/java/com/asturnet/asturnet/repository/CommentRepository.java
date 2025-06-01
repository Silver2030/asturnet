package com.asturnet.asturnet.repository;

import com.asturnet.asturnet.model.Comment;
import com.asturnet.asturnet.model.Post; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostOrderByCreatedAtAsc(Post post);

    long countByPost(Post post);
}