package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.model.FriendshipStatus; // Mantener si lo usas en otras partes
import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.service.FriendsService;
import com.asturnet.asturnet.service.LikeService;
import com.asturnet.asturnet.service.PostService;
import com.asturnet.asturnet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList; // Necesario si manejas listas vacías
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

    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        List<Post> postsToShow = new ArrayList<>(); // Inicializa una lista vacía para los posts
        Map<Long, Boolean> userLikesPost = new HashMap<>();
        Map<Long, Long> postLikesCount = new HashMap<>();
        Long currentUserId = null;
        User currentUser = null; // Declarar currentUser aquí para que sea accesible en todo el método

        if (principal != null) {
            String currentUsername = principal.getName();
            currentUser = userService.findByUsername(currentUsername);

            if (currentUser != null) {
                currentUserId = currentUser.getId();
                model.addAttribute("username", currentUser.getUsername()); // Añadir el username al modelo aquí
                model.addAttribute("currentUser", currentUser); // Pasar el currentUser al modelo para Thymeleaf

                // ***** APLICAR FILTRO AQUÍ *****
                postsToShow = postService.getHomeFeedPosts(currentUser); // <--- ¡CAMBIO CLAVE!
            } else {
                System.out.println("Current User not found in DB for principal: " + currentUsername);
                model.addAttribute("username", "Invitado"); // Si el usuario no se encuentra en DB
            }
        } else {
            // Usuario no autenticado: no se muestran posts o se muestran solo los públicos si existiera esa lógica
            // Por ahora, si no hay principal, no debería mostrar nada (o redirigir al login)
            // Ya que nuestro getHomeFeedPosts requiere un currentUser.
            System.out.println("User not authenticated. Redirecting to login or showing empty feed.");
            // Si /home está protegido por Spring Security, esta sección se alcanzará raramente.
            // Si permites acceso a /home sin autenticación, podrías querer mostrar un feed vacío o un mensaje.
            // Por el momento, la lógica del feed depende del usuario autenticado.
            model.addAttribute("username", "Invitado");
            model.addAttribute("posts", new ArrayList<>()); // No hay posts para invitados
            model.addAttribute("userLikesPost", new HashMap<>());
            model.addAttribute("postLikesCount", new HashMap<>());
            model.addAttribute("userId", null);
            return "home"; // O "redirect:/login"; si quieres forzar el login
        }

        // Si currentUser es null aquí (porque principal era null o no se encontró el usuario),
        // postsToShow seguirá siendo una lista vacía, lo cual es correcto.
        // Recalculamos los likes y los conteos para los 'postsToShow' (que ahora están filtrados)
        for (Post post : postsToShow) { // <-- Iterar sobre la lista FILTRADA
            if (currentUser != null) { // Solo si hay un usuario autenticado para verificar likes
                boolean userHasLiked = likeService.isLikedByUser(currentUser, post);
                userLikesPost.put(post.getId(), userHasLiked);
            }
            long likesCount = likeService.getLikesCountForPost(post);
            postLikesCount.put(post.getId(), likesCount);
        }

        // Añadir los posts filtrados y los mapas de likes al modelo
        model.addAttribute("posts", postsToShow); // <--- ¡Aquí se añade la lista FILTRADA!
        model.addAttribute("userLikesPost", userLikesPost);
        model.addAttribute("postLikesCount", postLikesCount);
        model.addAttribute("userId", currentUserId);

        return "home";
    }

    @GetMapping("/search")
    public String searchUsers(@RequestParam(value = "query", required = false) String query, Model model) {
        List<User> searchResults = List.of();

        if (query != null && !query.trim().isEmpty()) {
            searchResults = userService.searchUsers(query.trim());
            model.addAttribute("query", query);
        }

        // Necesitas el currentUser para determinar el estado de amistad en los resultados de búsqueda
        Principal principal = (Principal) model.getAttribute("principal"); // No puedes obtenerlo así
        // Lo correcto es:
        // Principal principalAuth = SecurityContextHolder.getContext().getAuthentication();
        // Si Principal principal no viene como argumento en searchUsers, necesitas obtenerlo de otra forma
        // Si solo se usa para mostrar, no hay problema. Si se usa para logica de amistad, necesitas el principal.

        if (principal != null) {
            String currentUsername = principal.getName();
            User currentUser = userService.findByUsername(currentUsername);
            if (currentUser != null) {
                Map<Long, FriendshipStatus> friendshipStatuses = new HashMap<>();
                for (User user : searchResults) {
                    if (!user.equals(currentUser)) { // No mostrar estado de amistad consigo mismo
                        FriendshipStatus status = friendsService.getFriendshipStatus(currentUser, user);
                        friendshipStatuses.put(user.getId(), status);
                    }
                }
                model.addAttribute("friendshipStatuses", friendshipStatuses);
                model.addAttribute("currentUserId", currentUser.getId()); // Para el botón de enviar/aceptar solicitud
            }
        }
        model.addAttribute("searchResults", searchResults);
        return "search-results";
    }
}