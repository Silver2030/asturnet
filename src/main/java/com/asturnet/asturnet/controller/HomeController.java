package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.model.FriendshipStatus;
import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.service.FriendsService;
import com.asturnet.asturnet.service.LikeService;
import com.asturnet.asturnet.service.PostService;
import com.asturnet.asturnet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication; // Necesario para SecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolder; // Necesario para SecurityContextHolder
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Necesario para ROLE_ADMIN
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final PostService postService;
    private final UserService userService;
    private final FriendsService friendsService;
    private final LikeService likeService;

    @Autowired
    public HomeController(PostService postService, UserService userService, FriendsService friendsService, LikeService likeService) {
        this.postService = postService;
        this.userService = userService;
        this.friendsService = friendsService;
        this.likeService = likeService;
    }

    // Método auxiliar para verificar si el usuario actual es admin
    // Este método ya lo tienes en PostController, pero lo duplicamos aquí para la lógica de visualización
    // (o podrías tener un servicio de utilidad de seguridad si se usa mucho)
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
               authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        List<Post> postsToShow = new ArrayList<>();
        Map<Long, Boolean> userLikesPost = new HashMap<>();
        Map<Long, Long> postLikesCount = new HashMap<>();
        Long currentUserId = null;
        User currentUser = null;

        // Añadir isAdmin al modelo de forma temprana
        model.addAttribute("isAdmin", isAdmin()); // <--- ¡Añadido aquí!

        if (principal != null) {
            String currentUsername = principal.getName();
            currentUser = userService.findByUsername(currentUsername);

            if (currentUser != null) {
                currentUserId = currentUser.getId();
                model.addAttribute("username", currentUser.getUsername());
                model.addAttribute("currentUser", currentUser);

                // Aplicar filtro de posts
                postsToShow = postService.getHomeFeedPosts(currentUser);
            } else {
                System.out.println("Current User not found in DB for principal: " + currentUsername);
                model.addAttribute("username", "Invitado");
            }
        } else {
            System.out.println("User not authenticated. Showing empty feed.");
            model.addAttribute("username", "Invitado");
            // Asegurarse de que todas las variables del modelo existan para evitar errores en Thymeleaf
            model.addAttribute("posts", new ArrayList<>());
            model.addAttribute("userLikesPost", new HashMap<>());
            model.addAttribute("postLikesCount", new HashMap<>());
            model.addAttribute("userId", null);
            return "home";
        }

        for (Post post : postsToShow) {
            if (currentUser != null) {
                boolean userHasLiked = likeService.isLikedByUser(currentUser, post);
                userLikesPost.put(post.getId(), userHasLiked);
            }
            long likesCount = likeService.getLikesCountForPost(post);
            postLikesCount.put(post.getId(), likesCount);
        }

        model.addAttribute("posts", postsToShow);
        model.addAttribute("userLikesPost", userLikesPost);
        model.addAttribute("postLikesCount", postLikesCount);
        model.addAttribute("userId", currentUserId);

        return "home";
    }

    @GetMapping("/search")
    public String searchUsers(@RequestParam(value = "query", required = false) String query, Model model, Principal principal) {
        List<User> searchResults = new ArrayList<>(); // Inicializar para evitar null
        
        // Asegúrate de pasar isAdmin al modelo de búsqueda también si el HTML de búsqueda usa encabezados o pie de página comunes
        model.addAttribute("isAdmin", isAdmin()); 

        if (query != null && !query.trim().isEmpty()) {
            searchResults = userService.searchUsers(query.trim());
            model.addAttribute("query", query);
        }

        // Obtener el principal directamente como argumento
        // Principal principal ya está disponible aquí por el parámetro del método
        if (principal != null) {
            String currentUsername = principal.getName();
            User currentUser = userService.findByUsername(currentUsername);
            if (currentUser != null) {
                Map<Long, FriendshipStatus> friendshipStatuses = new HashMap<>();
                for (User user : searchResults) {
                    if (!user.equals(currentUser)) {
                        FriendshipStatus status = friendsService.getFriendshipStatus(currentUser, user);
                        friendshipStatuses.put(user.getId(), status);
                    }
                }
                model.addAttribute("friendshipStatuses", friendshipStatuses);
                model.addAttribute("currentUserId", currentUser.getId());
            }
            model.addAttribute("currentUser", currentUser); // <--- Pasar currentUser al modelo de búsqueda
        } else {
            // Si no hay principal, el usuario no está autenticado. 
            // Asegúrate de que las variables del modelo necesarias en Thymeleaf existan.
            model.addAttribute("currentUser", null); // Para evitar NullPointer en Thymeleaf
            model.addAttribute("currentUserId", null);
            model.addAttribute("friendshipStatuses", new HashMap<>());
        }
        
        model.addAttribute("searchResults", searchResults);
        return "search-results";
    }
}