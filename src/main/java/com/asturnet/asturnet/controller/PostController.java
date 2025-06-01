package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.model.Comment;
import com.asturnet.asturnet.service.PostService;
import com.asturnet.asturnet.service.UserService;
import com.asturnet.asturnet.service.LikeService;
import com.asturnet.asturnet.service.CommentService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Controller
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final LikeService likeService;
    private final CommentService commentService;

    public PostController(PostService postService, UserService userService, LikeService likeService, CommentService commentService) {
        this.postService = postService;
        this.userService = userService;
        this.likeService = likeService;
        this.commentService = commentService;
    }

    // Método auxiliar para verificar si el usuario actual es admin
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
               authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    // Endpoint para la creación de posts
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

    // Endpoint para remover posts
    @PostMapping("/posts/delete")
    public String deletePost(@AuthenticationPrincipal UserDetails currentUser,
                             @RequestParam("postId") Long postId,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(currentUser.getUsername());
            Post post = postService.getPostById(postId);

            // Si el usuario actual es el propietario del post O si es un ADMIN
            if (post.getUser().getId().equals(user.getId()) || isAdmin()) {
                postService.deletePost(postId, user); 
                redirectAttributes.addFlashAttribute("successMessage", "Publicación eliminada exitosamente!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para eliminar esta publicación.");
            }

        } catch (RuntimeException e) { 
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar publicación: " + e.getMessage());
        }
        return "redirect:/home";
    }

    // Endpoint para la funcion de likes en los posts
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

    // Endpoint para la creación de comentarios
    @PostMapping("/comments/create")
    public String createComment(@AuthenticationPrincipal UserDetails currentUser,
                                 @RequestParam("postId") Long postId,
                                 @RequestParam("content") String content,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(currentUser.getUsername());
            Post post = postService.getPostById(postId);
            commentService.createComment(user, post, content);
            redirectAttributes.addFlashAttribute("successMessage", "Comentario publicado exitosamente!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al publicar comentario: " + e.getMessage());
        }
        return "redirect:/home";
    }

    // Endpoint para remover comentarios
    @PostMapping("/comments/delete")
    public String deleteComment(@AuthenticationPrincipal UserDetails currentUser,
                                 @RequestParam("commentId") Long commentId,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(currentUser.getUsername());
            Comment comment = commentService.getCommentById(commentId); 

            // Si el usuario actual es el propietario del comentario O si es un ADMIN
            if (comment.getUser().getId().equals(user.getId()) || isAdmin()) {
                commentService.deleteComment(commentId, user);
                redirectAttributes.addFlashAttribute("successMessage", "Comentario eliminado exitosamente!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para eliminar este comentario.");
            }

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar comentario: " + e.getMessage());
        }
        return "redirect:/home";
    }

    // Endpoint para obtener un post en particular
    @GetMapping("/posts/{id}")
    public String viewPostDetails(@PathVariable Long id, Model model) {
        Post post = postService.getPostById(id);

        model.addAttribute("post", post);
        return "post-details"; 
    }
}