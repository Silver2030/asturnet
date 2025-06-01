package com.asturnet.asturnet.repository;

import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByOrderByCreatedAtDesc();
    List<Post> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT p FROM Post p JOIN FETCH p.user u LEFT JOIN FETCH p.comments c " +
           "WHERE p.user = :currentUser " +
           "OR p.user IN :friendAuthors " +
           "OR u.isPrivate = FALSE " +
           "ORDER BY p.createdAt DESC")
    List<Post> findHomeFeedPosts(
        @Param("currentUser") User currentUser,
        @Param("friendAuthors") List<User> friendAuthors);

    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.comments ORDER BY p.createdAt DESC")
    List<Post> findAllWithUserAndCommentsOrderedByCreatedAtDesc();
}