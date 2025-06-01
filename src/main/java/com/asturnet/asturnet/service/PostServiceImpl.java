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

// IMPORTS NECESARIOS PARA VERIFICAR ROL DE ADMIN
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Correcto

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections; // Añadir esta importación si no está
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

    // Método auxiliar para verificar si el usuario actual es admin
    // Este método está perfecto y lo usaremos.
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
               authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
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

        // --- Lógica de autorización mejorada en el servicio ---
        // El usuario puede eliminar si es el propietario del post O si es un ADMIN
        if (post.getUser().getId().equals(currentUser.getId()) || isAdmin()) {
            postRepository.delete(post);
        } else {
            throw new RuntimeException("No tienes permiso para eliminar esta publicación.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Post> getAllPostsWithUserAndComments() {
        return postRepository.findAllWithUserAndCommentsOrderedByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Post> getHomeFeedPosts(User currentUser) {
        // *** PASO CRÍTICO: Si el usuario es administrador, devuelve TODOS los posts ***
        if (isAdmin()) { // Usamos el método auxiliar isAdmin()
            return postRepository.findByOrderByCreatedAtDesc(); // Este método debe traer todos los posts.
        }

        // Lógica para usuarios normales (no administradores y autenticados)
        // Obtener IDs de amigos
        Set<Long> friendIds = new HashSet<>();
        friendIds.add(currentUser.getId()); // Incluir al propio usuario para ver sus posts

        // Obtener amigos del usuario actual
        // El código de abajo es un poco redundante si FriendshipService ya tiene getAcceptedFriends
        // Deberías usar: List<User> friends = friendshipService.getAcceptedFriends(currentUser.getId());
        // Y luego extraer los IDs.
        // Pero adaptaremos tu lógica existente.

        List<Friends> friendshipsWhereUserIsSender = friendsRepository.findByUserAndStatus(currentUser, FriendshipStatus.ACCEPTED);
        for (Friends friends : friendshipsWhereUserIsSender) {
            friendIds.add(friends.getFriend().getId());
        }

        List<Friends> friendshipsWhereUserIsReceiver = friendsRepository.findByFriendAndStatus(currentUser, FriendshipStatus.ACCEPTED);
        for (Friends friends : friendshipsWhereUserIsReceiver) {
            friendIds.add(friends.getUser().getId());
        }

        // Recuperar los objetos User de los amigos
        List<User> friendAuthors = new ArrayList<>();
        if (!friendIds.isEmpty()) {
            friendAuthors = userRepository.findAllById(friendIds);
        }

        // Llamar al repositorio con la lógica de filtrado combinada
        // Esto asume que findHomeFeedPosts en el repositorio puede manejar la lógica
        // de "posts de amigos O posts públicos".
        return postRepository.findHomeFeedPosts(currentUser, friendAuthors);
    }
}