package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.model.Friends;
import com.asturnet.asturnet.model.FriendshipStatus; // Asegúrate de que este enum está importado
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

    // --- Método para ver el perfil de OTRO usuario ---
    @GetMapping("/profile/{username}")
    public String viewOtherUserProfile(@PathVariable String username, Model model,
                                       @AuthenticationPrincipal UserDetails currentUserDetails,
                                       RedirectAttributes redirectAttributes) {
        if (currentUserDetails == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(currentUserDetails.getUsername());
        User otherUser = userService.findByUsername(username);

        if (otherUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.");
            return "redirect:/home";
        }

        if (currentUser.getId().equals(otherUser.getId())) {
            return "redirect:/profile"; // Redirige al controlador del propio perfil (en UserController)
        }

        model.addAttribute("user", otherUser);
        model.addAttribute("isCurrentUser", false);

        // Lógica para determinar el estado de amistad
        FriendshipStatus status = friendsService.getFriendshipStatus(currentUser, otherUser);
        model.addAttribute("friendshipStatus", status);
        model.addAttribute("isFriend", status == FriendshipStatus.ACCEPTED);

        // Lógica para diferenciar si la solicitud pendiente fue enviada o recibida
        boolean sentPendingRequest = false;
        boolean receivedPendingRequest = false;
        if (status == FriendshipStatus.PENDING) {
            Optional<Friends> friendship = friendsService.findFriendsBetween(currentUser, otherUser);
            if (friendship.isPresent()) {
                if (friendship.get().getUser().getId().equals(currentUser.getId())) {
                    sentPendingRequest = true; // El usuario actual es el 'user' (remitente) en la relación de amistad
                } else if (friendship.get().getFriend().getId().equals(currentUser.getId())) {
                    receivedPendingRequest = true; // El usuario actual es el 'friend' (receptor) en la relación de amistad
                }
            }
        }
        model.addAttribute("sentPendingRequest", sentPendingRequest);
        model.addAttribute("receivedPendingRequest", receivedPendingRequest);

        // Lógica para mostrar posts basada en la privacidad
        if (otherUser.getIsPrivate() && status != FriendshipStatus.ACCEPTED) {
            model.addAttribute("isPrivateAndNotFriend", true);
            model.addAttribute("userPosts", new java.util.ArrayList<>());
        } else {
            model.addAttribute("isPrivateAndNotFriend", false);
            // *** ¡CORRECCIÓN APLICADA AQUÍ! ***
            // Usamos el método getPostsByUser(User user) que ya existe en tu PostService
            List<Post> userPosts = postService.getPostsByUser(otherUser);
            model.addAttribute("userPosts", userPosts);
        }

        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.getAttribute("successMessage"));
        }
        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.getAttribute("errorMessage"));
        }

        return "profile";
    }

    // --- Métodos para la GESTIÓN de AMISTAD ---

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

    @PostMapping("/friends/acceptRequest")
    public String acceptFriendRequest(@RequestParam("requesterId") Long requesterId,
                                      @AuthenticationPrincipal UserDetails currentUserDetails,
                                      RedirectAttributes redirectAttributes) {
        try {
            User acceptor = userService.findByUsername(currentUserDetails.getUsername());
            User requester = userService.findById(requesterId)
                                         .orElseThrow(() -> new RuntimeException("Solicitante no encontrado."));
            friendsService.acceptFriendRequest(acceptor, requester);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud de amistad aceptada. ¡Ahora sois amigos!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al aceptar solicitud: " + e.getMessage());
        }
        User requesterUser = userService.findById(requesterId).orElse(null);
        if (requesterUser != null) {
            return "redirect:/profile/" + requesterUser.getUsername();
        }
        return "redirect:/profile";
    }

    @PostMapping("/friends/declineRequest")
    public String declineFriendRequest(@RequestParam("requesterId") Long requesterId,
                                       @AuthenticationPrincipal UserDetails currentUserDetails,
                                       RedirectAttributes redirectAttributes) {
        try {
            User decliner = userService.findByUsername(currentUserDetails.getUsername());
            User requester = userService.findById(requesterId)
                                         .orElseThrow(() -> new RuntimeException("Solicitante no encontrado."));
            friendsService.declineFriendRequest(decliner, requester);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud de amistad rechazada.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al rechazar solicitud: " + e.getMessage());
        }
        User requesterUser = userService.findById(requesterId).orElse(null);
        if (requesterUser != null) {
            return "redirect:/profile/" + requesterUser.getUsername();
        }
        return "redirect:/profile";
    }

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