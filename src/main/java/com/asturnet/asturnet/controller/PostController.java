package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.model.Like;
import com.asturnet.asturnet.model.Comment; // <-- IMPORTANTE: Importar Comment
import com.asturnet.asturnet.service.PostService;
import com.asturnet.asturnet.service.UserService;
import com.asturnet.asturnet.service.LikeService;
import com.asturnet.asturnet.service.CommentService; // <-- IMPORTANTE: Importar CommentService

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final LikeService likeService;
    private final CommentService commentService; // <-- IMPORTANTE: Inyectamos CommentService

    public PostController(PostService postService, UserService userService, LikeService likeService, CommentService commentService) { // <-- CONSTRUCTOR ACTUALIZADO
        this.postService = postService;
        this.userService = userService;
        this.likeService = likeService;
        this.commentService = commentService; // <-- Asignamos CommentService
    }

    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        User user = null;
        Map<Long, Boolean> userLikesPost = new HashMap<>();
        Map<Long, Long> postLikesCount = new HashMap<>();
        // <-- IMPORTANTE: Mapa para los comentarios de cada post
        Map<Long, List<Comment>> postComments = new HashMap<>();


        if (currentUser != null) {
            user = userService.findByUsername(currentUser.getUsername());
            model.addAttribute("username", user.getUsername());
            model.addAttribute("userId", user.getId());
        }

        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);

        if (user != null) {
            for (Post post : posts) {
                userLikesPost.put(post.getId(), likeService.isLikedByUser(user, post));
                postLikesCount.put(post.getId(), likeService.getLikesCountForPost(post));
                // <-- IMPORTANTE: Obtener comentarios para cada post
                postComments.put(post.getId(), commentService.getCommentsByPost(post));
            }
        } else {
            for (Post post : posts) {
                postLikesCount.put(post.getId(), likeService.getLikesCountForPost(post));
                // <-- IMPORTANTE: Obtener comentarios para cada post incluso si no hay usuario logueado
                postComments.put(post.getId(), commentService.getCommentsByPost(post));
            }
        }
        model.addAttribute("userLikesPost", userLikesPost);
        model.addAttribute("postLikesCount", postLikesCount);
        model.addAttribute("postComments", postComments); // <-- IMPORTANTE: Pasa el mapa de comentarios

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

    @PostMapping("/posts/toggleLike")
    public String toggleLike(@AuthenticationPrincipal UserDetails currentUser,
                             @RequestParam("postId") Long postId,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(currentUser.getUsername());
            Post post = postService.getPostById(postId);
            likeService.toggleLike(user, post);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar like: " + e.getMessage());
        }
        return "redirect:/home";
    }

    // <-- IMPORTANTE: Nuevo endpoint para crear un comentario
    @PostMapping("/comments/create")
    public String createComment(@AuthenticationPrincipal UserDetails currentUser,
                                @RequestParam("postId") Long postId,
                                @RequestParam("content") String content,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(currentUser.getUsername());
            Post post = postService.getPostById(postId); // Asegurarse de que el post existe
            commentService.createComment(user, post, content);
            redirectAttributes.addFlashAttribute("successMessage", "Comentario publicado exitosamente!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al publicar comentario: " + e.getMessage());
        }
        return "redirect:/home"; // Redirige de vuelta a la página de inicio
    }

    // <-- IMPORTANTE: Nuevo endpoint para eliminar un comentario
    @PostMapping("/comments/delete")
    public String deleteComment(@AuthenticationPrincipal UserDetails currentUser,
                                @RequestParam("commentId") Long commentId,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(currentUser.getUsername());
            commentService.deleteComment(commentId, user);
            redirectAttributes.addFlashAttribute("successMessage", "Comentario eliminado exitosamente!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar comentario: " + e.getMessage());
        }
        return "redirect:/home"; // Redirige de vuelta a la página de inicio
    }
}