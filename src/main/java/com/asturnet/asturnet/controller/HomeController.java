package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.model.FriendshipStatus;
import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.service.FriendsService; // Importa el servicio de amistad
import com.asturnet.asturnet.service.PostService;
import com.asturnet.asturnet.service.UserService; // Importa el servicio de usuario
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal; // Necesitas esto para obtener el usuario autenticado
import java.util.List;
import java.util.stream.Collectors; // Para usar streams

@Controller
public class HomeController {

    private final PostService postService;
    private final UserService userService; // Inyecta UserService
    private final FriendsService friendsService; // Inyecta FriendshipService

    @Autowired
    public HomeController(PostService postService, UserService userService, FriendsService friendsService) {
        this.postService = postService;
        this.userService = userService;
        this.friendsService = friendsService;
    }

    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        // *** CAMBIO AQUÍ: Declarar currentUser dentro del if, y usar un final para el filtro ***
        final User finalCurrentUser; // Declaramos una variable final

        if (principal != null) {
            String currentUsername = principal.getName();
            // Asignamos el valor a la variable final
            finalCurrentUser = userService.findByUsername(currentUsername);
            model.addAttribute("currentUser", finalCurrentUser); // Añade el currentUser al modelo
        } else {
            finalCurrentUser = null; // Si no hay principal, inicializamos a null
        }

        // Obtén todas las publicaciones
        List<Post> allPosts = postService.findAllPostsOrderedByCreatedAtDesc();

        // Filtrar las publicaciones
        List<Post> filteredPosts = allPosts.stream()
            .filter(post -> {
                User postAuthor = post.getUser();

                // 1. Si el autor del post es el usuario actual, siempre mostrar el post.
                // Usamos finalCurrentUser que es effectively final
                if (finalCurrentUser != null && postAuthor.getId().equals(finalCurrentUser.getId())) {
                    return true;
                }

                // 2. Si el perfil del autor es público, siempre mostrar el post.
                if (!postAuthor.getIsPrivate()) {
                    return true;
                }

                // 3. Si el perfil del autor es privado:
                //    - Solo mostrar el post si el usuario actual y el autor son amigos.
                // Usamos finalCurrentUser que es effectively final
                if (finalCurrentUser != null && postAuthor.getIsPrivate()) {
                    FriendshipStatus status = friendsService.getFriendshipStatus(finalCurrentUser, postAuthor);
                    return status == FriendshipStatus.ACCEPTED;
                }

                // Si ninguna de las condiciones anteriores se cumple (ej. perfil privado y no amigos), no mostrar.
                return false;
            })
            .collect(Collectors.toList());

        model.addAttribute("posts", filteredPosts);

        // Mensajes flash (si los usas)
        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.getAttribute("successMessage"));
        }
        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.getAttribute("errorMessage"));
        }

        return "home";
    }

    // Otros métodos del controlador...
}