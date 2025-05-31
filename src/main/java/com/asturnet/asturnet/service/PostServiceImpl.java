package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.model.Friends;
import com.asturnet.asturnet.model.FriendshipStatus;
import com.asturnet.asturnet.repository.PostRepository;
import com.asturnet.asturnet.repository.UserRepository;
import com.asturnet.asturnet.repository.FriendsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FriendsRepository friendsRepository;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository, FriendsRepository friendsRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.friendsRepository = friendsRepository;
    }

    @Override
    @Transactional
    public Post createPost(User user, String content, String imageUrl, String videoUrl) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("El contenido de la publicación no puede estar vacío.");
        }
        Post post = new Post();
        post.setUser(user);
        post.setContent(content);
        post.setImageUrl(imageUrl);
        post.setVideoUrl(videoUrl);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    @Override
    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada con ID: " + postId));
    }

    @Override
    public List<Post> getAllPosts() {
        return postRepository.findByOrderByCreatedAtDesc();
    }

    @Override
    public List<Post> getPostsByUser(User user) {
        return postRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada con ID: " + postId));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar esta publicación.");
        }

        postRepository.delete(post);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Post> getAllPostsWithUserAndComments() {
        return postRepository.findAllWithUserAndCommentsOrderedByCreatedAtDesc();
    }

    // ***** IMPLEMENTACIÓN CORREGIDA DEL MÉTODO getHomeFeedPosts *****
    @Override
    @Transactional(readOnly = true)
    public List<Post> getHomeFeedPosts(User currentUser) {
        Set<Long> friendIds = new HashSet<>();

        List<Friends> friendshipsWhereUserIsSender = friendsRepository.findByUserAndStatus(currentUser, FriendshipStatus.ACCEPTED);
        for (Friends friends : friendshipsWhereUserIsSender) {
            friendIds.add(friends.getFriend().getId());
        }

        List<Friends> friendshipsWhereUserIsReceiver = friendsRepository.findByFriendAndStatus(currentUser, FriendshipStatus.ACCEPTED);
        for (Friends friends : friendshipsWhereUserIsReceiver) {
            friendIds.add(friends.getUser().getId());
        }

        List<User> friendAuthors = new ArrayList<>();
        if (!friendIds.isEmpty()) {
            friendAuthors = userRepository.findAllById(friendIds);
        }

        // *** LA LÍNEA QUE GENERA EL ERROR Y DEBE SER REEMPLAZADA ES ESTA: ***
        // return postRepository.findByUserInWithUserAndCommentsOrderByCreatedAtDesc(authorsToFetch);
        // *** DEBE SER ESTA OTRA: ***
        return postRepository.findHomeFeedPosts(currentUser, friendAuthors); // <-- ¡CORRECCIÓN AQUÍ!
    }
}