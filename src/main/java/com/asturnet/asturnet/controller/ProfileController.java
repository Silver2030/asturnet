package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.model.FriendshipStatus;
import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.service.FriendsService;
import com.asturnet.asturnet.service.PostService;
import com.asturnet.asturnet.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.Collections;

@Controller
public class ProfileController {

    private final UserService userService;
    private final FriendsService friendsService;
    private final PostService postService;

    public ProfileController(UserService userService, FriendsService friendsService, PostService postService) {
        this.userService = userService;
        this.friendsService = friendsService;
        this.postService = postService;
    }

    // Endpoint para obtener el perfil propio
    @GetMapping("/profile")
    public String viewMyProfile(@AuthenticationPrincipal UserDetails currentUserDetails) {
        if (currentUserDetails == null) {
            return "redirect:/login";
        }
        User currentUser = userService.findByUsername(currentUserDetails.getUsername());
        if (currentUser == null) {
            return "redirect:/logout";
        }
        return "redirect:/profile/" + currentUser.getUsername();
    }

    // Endpoint para obtener un perfil ajeno
   @GetMapping("/profile/{username}")
    public String viewOtherUserProfile(@PathVariable String username, Model model,
                                       @AuthenticationPrincipal UserDetails currentUserDetails,
                                       RedirectAttributes redirectAttributes) {
        if (currentUserDetails == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(currentUserDetails.getUsername());
        User profileUser = userService.findByUsername(username);

        if (profileUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.");
            return "redirect:/home";
        }

        model.addAttribute("user", profileUser); // El usuario del perfil que se está viendo
        model.addAttribute("currentUser", currentUser); // El usuario autenticado

        boolean isCurrentUser = currentUser.getId().equals(profileUser.getId());
        model.addAttribute("isCurrentUser", isCurrentUser);

        // Lógica para el rol de administrador
        boolean isAdmin = currentUserDetails.getAuthorities().stream()
                                             .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin); // Añadir isAdmin al modelo por si se usa en la vista

        FriendshipStatus status = null;
        if (!isCurrentUser) { // Si NO es el propio usuario, obtenemos el estado de amistad real
            status = friendsService.getFriendshipStatus(currentUser, profileUser);
        } else {
            status = null; 
        }
        model.addAttribute("friendshipStatus", status);

        model.addAttribute("isFriend", status == FriendshipStatus.ACCEPTED); 

        boolean sentPendingRequest = false;
        boolean receivedPendingRequest = false;
        
        // Solo verificamos solicitudes pendientes si no es el usuario actual y el estado es PENDING
        if (!isCurrentUser && status == FriendshipStatus.PENDING) {
            // Usamos el método unidireccional para saber quién envió la solicitud PENDING
            Optional<com.asturnet.asturnet.model.Friends> friendship = friendsService.findByUserAndFriend(currentUser, profileUser);
            if (friendship.isPresent() && friendship.get().getStatus() == FriendshipStatus.PENDING) {
                sentPendingRequest = true;
            } else {
                Optional<com.asturnet.asturnet.model.Friends> reverseFriendship = friendsService.findByUserAndFriend(profileUser, currentUser);
                if (reverseFriendship.isPresent() && reverseFriendship.get().getStatus() == FriendshipStatus.PENDING) {
                    receivedPendingRequest = true;
                }
            }
        }
        model.addAttribute("sentPendingRequest", sentPendingRequest);
        model.addAttribute("receivedPendingRequest", receivedPendingRequest);

        // Lógica para determinar si el perfil es privado y no visible
        boolean isPrivateAndNotFriend = profileUser.getIsPrivate() && !isCurrentUser && status != FriendshipStatus.ACCEPTED && !isAdmin;
        model.addAttribute("isPrivateAndNotFriend", isPrivateAndNotFriend);

        List<Post> userPosts;
        List<User> friendsList;

        if (!isPrivateAndNotFriend) { // Si no está bloqueado por privacidad (incluye admins y amigos)
            userPosts = postService.getPostsByUser(profileUser);
            friendsList = friendsService.getAcceptedFriends(profileUser);
        } else {
            userPosts = Collections.emptyList();
            friendsList = Collections.emptyList();
        }
        model.addAttribute("userPosts", userPosts);
        model.addAttribute("friendsList", friendsList);

        if (model.asMap().containsKey("successMessage")) { 
            model.addAttribute("successMessage", model.asMap().get("successMessage"));
        }
        if (model.asMap().containsKey("errorMessage")) {
            model.addAttribute("errorMessage", model.asMap().get("errorMessage"));
        }

        return "profile"; 
    }

    // Endpoint para enviar solicitudes de amistad
    @PostMapping("/friends/sendRequest")
    public String sendFriendRequest(@RequestParam("receiverId") Long receiverId,
                                    @AuthenticationPrincipal UserDetails currentUserDetails,
                                    RedirectAttributes redirectAttributes) {
        try {
            User sender = userService.findByUsername(currentUserDetails.getUsername());
            User receiver = userService.findById(receiverId)
                                     .orElseThrow(() -> new RuntimeException("Receptor no encontrado."));
            friendsService.sendFriendRequest(sender, receiver);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud de amistad enviada.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al enviar solicitud: " + e.getMessage());
        }
        User receiverUser = userService.findById(receiverId).orElse(null);
        if (receiverUser != null) {
            return "redirect:/profile/" + receiverUser.getUsername();
        }
        return "redirect:/home";
    }

    // Endpoint para cancelar solicitudes de amistad
    @PostMapping("/friends/cancelRequest")
    public String cancelFriendRequest(@RequestParam("receiverId") Long receiverId,
                                      @AuthenticationPrincipal UserDetails currentUserDetails,
                                      RedirectAttributes redirectAttributes) {
        try {
            User sender = userService.findByUsername(currentUserDetails.getUsername());
            User receiver = userService.findById(receiverId)
                                     .orElseThrow(() -> new RuntimeException("Receptor no encontrado."));
            friendsService.cancelFriendRequest(sender, receiver);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud de amistad cancelada.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cancelar solicitud: " + e.getMessage());
        }
        User receiverUser = userService.findById(receiverId).orElse(null);
        if (receiverUser != null) {
            return "redirect:/profile/" + receiverUser.getUsername();
        }
        return "redirect:/home";
    }

    // Endpoint para aceptar solicitudes de amistad
    @PostMapping("/friends/acceptRequest")
    public String acceptFriendRequest(@RequestParam("requesterId") Long requesterId,
                                      @AuthenticationPrincipal UserDetails currentUserDetails,
                                      RedirectAttributes redirectAttributes,
                                      @RequestParam(value = "fromRequestsPage", defaultValue = "false") boolean fromRequestsPage) {
        try {
            User acceptor = userService.findByUsername(currentUserDetails.getUsername());
            User requester = userService.findById(requesterId)
                                         .orElseThrow(() -> new RuntimeException("Solicitante no encontrado."));
            friendsService.acceptFriendRequest(acceptor, requester);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud de amistad aceptada. ¡Ahora sois amigos!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al aceptar solicitud: " + e.getMessage());
        }

        if (fromRequestsPage) {
            return "redirect:/friend-requests";
        } else {
            User requesterUser = userService.findById(requesterId).orElse(null);
            if (requesterUser != null) {
                return "redirect:/profile/" + requesterUser.getUsername();
            }
            return "redirect:/profile";
        }
    }

    // Endpoint para rechazar solicitudes de amistad
    @PostMapping("/friends/declineRequest")
    public String declineFriendRequest(@RequestParam("requesterId") Long requesterId,
                                       @AuthenticationPrincipal UserDetails currentUserDetails,
                                       RedirectAttributes redirectAttributes,
                                       @RequestParam(value = "fromRequestsPage", defaultValue = "false") boolean fromRequestsPage) {
        try {
            User decliner = userService.findByUsername(currentUserDetails.getUsername());
            User requester = userService.findById(requesterId)
                                         .orElseThrow(() -> new RuntimeException("Solicitante no encontrado."));
            friendsService.declineFriendRequest(decliner, requester);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud de amistad rechazada.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al rechazar solicitud: " + e.getMessage());
        }

        if (fromRequestsPage) {
            return "redirect:/friend-requests";
        } else {
            User requesterUser = userService.findById(requesterId).orElse(null);
            if (requesterUser != null) {
                return "redirect:/profile/" + requesterUser.getUsername();
            }
            return "redirect:/profile";
        }
    }

    // Endpoint para remover amigos
    @PostMapping("/friends/removeFriend")
    public String removeFriend(@RequestParam("friendId") Long friendId,
                               @AuthenticationPrincipal UserDetails currentUserDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            User user1 = userService.findByUsername(currentUserDetails.getUsername());
            User user2 = userService.findById(friendId)
                                   .orElseThrow(() -> new RuntimeException("Amigo no encontrado."));
            friendsService.removeFriend(user1, user2);
            redirectAttributes.addFlashAttribute("successMessage", "Amigo eliminado.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar amigo: " + e.getMessage());
        }
        User removedFriendUser = userService.findById(friendId).orElse(null);
        if (removedFriendUser != null) {
            return "redirect:/profile/" + removedFriendUser.getUsername();
        }
        return "redirect:/profile";
    }
}