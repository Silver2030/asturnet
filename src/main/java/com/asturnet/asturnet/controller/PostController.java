package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.model.Like; // Importar la entidad Like
import com.asturnet.asturnet.service.PostService;
import com.asturnet.asturnet.service.UserService;
import com.asturnet.asturnet.service.LikeService; // Importar LikeService
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map; // Para el mapa de likes
import java.util.HashMap; // Para el mapa de likes

@Controller
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final LikeService likeService; // Inyectamos LikeService

    public PostController(PostService postService, UserService userService, LikeService likeService) { // Constructor actualizado
        this.postService = postService;
        this.userService = userService;
        this.likeService = likeService; // Asignamos LikeService
    }

    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        User user = null;
        Map<Long, Boolean> userLikesPost = new HashMap<>(); // Mapa para saber si el usuario actual le dio like a cada post
        Map<Long, Long> postLikesCount = new HashMap<>(); // Mapa para el conteo de likes de cada post

        if (currentUser != null) {
            user = userService.findByUsername(currentUser.getUsername());
            model.addAttribute("username", user.getUsername());
            model.addAttribute("userId", user.getId());
        }

        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);

        if (user != null) { // Solo si hay un usuario logueado, verificamos sus likes y el conteo
            for (Post post : posts) {
                userLikesPost.put(post.getId(), likeService.isLikedByUser(user, post));
                postLikesCount.put(post.getId(), likeService.getLikesCountForPost(post));
            }
        } else { // Si no hay usuario logueado, aún podemos obtener el conteo de likes
            for (Post post : posts) {
                postLikesCount.put(post.getId(), likeService.getLikesCountForPost(post));
            }
        }
        model.addAttribute("userLikesPost", userLikesPost); // Pasa el mapa de likes del usuario
        model.addAttribute("postLikesCount", postLikesCount); // Pasa el mapa de conteo de likes


        model.addAttribute("postContent", "");
        model.addAttribute("postImageUrl", "");
        model.addAttribute("postVideoUrl", "");

        return "home";
    }

    @PostMapping("/posts/create")
    public String createPost(@AuthenticationPrincipal UserDetails currentUser,
                             @RequestParam("content") String content,
                             @RequestParam(value = "imageUrl", required = false) String imageUrl,
                             @RequestParam(value = "videoUrl", required = false) String videoUrl,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(currentUser.getUsername());
            postService.createPost(user, content, imageUrl, videoUrl);
            redirectAttributes.addFlashAttribute("successMessage", "Publicación creada exitosamente!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear publicación: " + e.getMessage());
        }
        return "redirect:/home";
    }

    @PostMapping("/posts/delete")
    public String deletePost(@AuthenticationPrincipal UserDetails currentUser,
                             @RequestParam("postId") Long postId,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(currentUser.getUsername());
            postService.deletePost(postId, user);
            redirectAttributes.addFlashAttribute("successMessage", "Publicación eliminada exitosamente!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar publicación: " + e.getMessage());
        }
        return "redirect:/home";
    }

    // Nuevo endpoint para dar/quitar like
    @PostMapping("/posts/toggleLike")
    public String toggleLike(@AuthenticationPrincipal UserDetails currentUser,
                             @RequestParam("postId") Long postId,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(currentUser.getUsername());
            Post post = postService.getPostById(postId); // Obtener el post
            likeService.toggleLike(user, post);
            // No hay mensaje de éxito/error aquí, ya que el like/dislike es inmediato
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar like: " + e.getMessage());
        }
        return "redirect:/home"; // Redirige de vuelta a la página de inicio
    }
}