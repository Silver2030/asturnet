package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.model.FriendshipStatus;
import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.service.FriendsService; // Importa el servicio de amistad
import com.asturnet.asturnet.service.LikeService;
import com.asturnet.asturnet.service.PostService;
import com.asturnet.asturnet.service.UserService; // Importa el servicio de usuario
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal; // Necesitas esto para obtener el usuario autenticado
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // Para usar streams

@Controller
public class HomeController {

    private final PostService postService;
    private final UserService userService; // Inyecta UserService
    private final FriendsService friendsService; // Inyecta FriendshipService
    private final LikeService likeService;

    @Autowired
    public HomeController(PostService postService, UserService userService, FriendsService friendsService, LikeService likeService) {
        this.postService = postService;
        this.userService = userService;
        this.friendsService = friendsService;
        this.likeService = likeService;
    }

   // En tu Home/PostController (o el controlador que sirve /home)
@GetMapping("/home")
public String home(Model model, Principal principal) {
    // 1. Obtener todos los posts (esto debería ir primero para procesar todos)
    List<Post> allPosts = postService.getAllPostsWithUserAndComments();
    model.addAttribute("posts", allPosts);

    // 2. Inicializar los mapas para likes (siempre deben existir en el modelo)
    Map<Long, Boolean> userLikesPost = new HashMap<>();
    Map<Long, Long> postLikesCount = new HashMap<>();
    Long currentUserId = null; // Inicializar a null por defecto

    // 3. Lógica relacionada con el usuario autenticado (si existe)
    if (principal != null) {
        String currentUsername = principal.getName();
        User currentUser = userService.findByUsername(currentUsername); // Asegúrate de que este usuario nunca sea null
        if (currentUser != null) {
            currentUserId = currentUser.getId(); // Solo asigna si hay usuario
            // No añadir userId al modelo aquí, se añade al final para consistencia si es necesario
            
            System.out.println("Current User ID: " + currentUserId); // Depuración

            for (Post post : allPosts) {
                // Asegúrate de que post.getId() no sea null aquí si usas IDs autogenerados
                boolean userHasLiked = likeService.isLikedByUser(currentUser, post);
                userLikesPost.put(post.getId(), userHasLiked);
                
                // Depuración
                System.out.println("  Post " + post.getId() + " - User " + currentUser.getId() + " liked: " + userHasLiked);
            }
        }
    } else {
        System.out.println("User not authenticated (Principal is null). No likes/delete actions shown.");
        // Si el usuario no está autenticado, los mapas userLikesPost y postLikesCount
        // seguirán siendo vacíos, lo cual es correcto.
    }

    // 4. Calcular los likes para TODOS los posts (esto puede ir fuera del if de principal)
    for (Post post : allPosts) {
        // Asegúrate de que post.getId() no sea null aquí
        long likesCount = likeService.getLikesCountForPost(post);
        postLikesCount.put(post.getId(), likesCount);
        
        // Depuración
        System.out.println("  Post " + post.getId() + " - Likes Count: " + likesCount);
    }
    // --- Fin de depuración ---

    // 5. Añadir TODAS las variables al modelo AL FINAL
    model.addAttribute("userLikesPost", userLikesPost);
    model.addAttribute("postLikesCount", postLikesCount);
    model.addAttribute("userId", currentUserId); // Siempre añade userId, será null si no hay usuario logueado

    // ... otros atributos del modelo, como 'username' ...
    if (principal != null) {
        model.addAttribute("username", principal.getName());
    } else {
        model.addAttribute("username", "Invitado"); // O un valor por defecto
    }

    return "home";
}

    @GetMapping("/search")
    public String searchUsers(@RequestParam(value = "query", required = false) String query, Model model) {
        List<User> searchResults = List.of(); // Inicializa como lista vacía

        if (query != null && !query.trim().isEmpty()) {
            searchResults = userService.searchUsers(query.trim());
            model.addAttribute("query", query); // Para mostrar la query en el input de búsqueda
        }

        model.addAttribute("searchResults", searchResults);
        return "search-results"; // Nombre de tu nueva plantilla Thymeleaf para resultados
    }

    // Otros métodos del controlador...
}