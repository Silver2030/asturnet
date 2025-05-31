package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.service.PostService;
import com.asturnet.asturnet.service.UserService; // Necesitamos UserService para obtener el usuario actual

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PostController {

    private final PostService postService;
    private final UserService userService; // Inyectamos UserService

    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    // Ruta para mostrar la página de inicio (feed de publicaciones)
    @GetMapping("/home") // Sobreescribe el mapeo /home si ya lo tenías en otro controlador
    public String home(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        // Obtenemos el usuario actual autenticado
        User user = null;
        if (currentUser != null) {
            user = userService.findByUsername(currentUser.getUsername());
            model.addAttribute("username", user.getUsername());
            model.addAttribute("userId", user.getId()); // Pasa el ID del usuario
        }

        // Obtener todas las publicaciones para el feed
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts); // Agrega la lista de posts al modelo

        // Para el formulario de creación de posts (opcional, pero buena práctica)
        model.addAttribute("postContent", "");
        model.addAttribute("postImageUrl", "");
        model.addAttribute("postVideoUrl", "");

        return "home"; // Devuelve la plantilla home.html
    }

    // Ruta para crear una nueva publicación
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
        return "redirect:/home"; // Redirige de vuelta a la página de inicio
    }

    // Ruta para eliminar una publicación
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
        return "redirect:/home"; // Redirige de vuelta a la página de inicio
    }
}