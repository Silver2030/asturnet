// src/main/java/com/asturnet/asturnet/controller/FriendRequestController.java

package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.model.Friends;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.service.FriendsService;
import com.asturnet.asturnet.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class FriendRequestController {

    private final FriendsService friendsService;
    private final UserService userService;

    public FriendRequestController(FriendsService friendsService, UserService userService) {
        this.friendsService = friendsService;
        this.userService = userService;
    }

    // Muestra la página con todas las solicitudes de amistad pendientes recibidas
    @GetMapping("/friend-requests")
    public String viewFriendRequests(Model model, @AuthenticationPrincipal UserDetails currentUserDetails) {
        if (currentUserDetails == null) {
            return "redirect:/login";
        }
        User currentUser = userService.findByUsername(currentUserDetails.getUsername());
        if (currentUser == null) {
            return "redirect:/logout"; // O a una página de error
        }

        List<Friends> pendingRequests = friendsService.getPendingFriendRequests(currentUser);
        model.addAttribute("pendingRequests", pendingRequests);

        // Mensajes flash para éxito/error
        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.getAttribute("successMessage"));
        }
        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.getAttribute("errorMessage"));
        }

        return "friend-requests"; // Nombre de la plantilla HTML
    }

    // Los métodos accept/declineRequest que ya tenías en ProfileController
    // Opcional: Podrías moverlos aquí si quieres centralizar la lógica de solicitudes
    // Por ahora, asumo que los dejarás en ProfileController si ya funcionan.
    // Si los mueves, asegúrate de cambiar los action de los formularios en friend-requests.html
    // para que apunten a este controlador.

    // EJEMPLO de cómo se vería si los movieras aquí (NO MOVER SI YA FUNCIONAN EN PROFILECONTROLLER):
    // @PostMapping("/friend-requests/accept")
    // public String acceptFriendRequestFromList(@RequestParam("requesterId") Long requesterId,
    //                                         @AuthenticationPrincipal UserDetails currentUserDetails,
    //                                         RedirectAttributes redirectAttributes) {
    //     // ... lógica de acceptFriendRequest ...
    //     return "redirect:/friend-requests"; // Redirige de vuelta a la lista de solicitudes
    // }

    // @PostMapping("/friend-requests/decline")
    // public String declineFriendRequestFromList(@RequestParam("requesterId") Long requesterId,
    //                                         @AuthenticationPrincipal UserDetails currentUserDetails,
    //                                         RedirectAttributes redirectAttributes) {
    //     // ... lógica de declineFriendRequest ...
    //     return "redirect:/friend-requests"; // Redirige de vuelta a la lista de solicitudes
    // }
}