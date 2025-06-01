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

import java.util.List;

@Controller
public class FriendRequestController {

    private final FriendsService friendsService;
    private final UserService userService;

    public FriendRequestController(FriendsService friendsService, UserService userService) {
        this.friendsService = friendsService;
        this.userService = userService;
    }

    // Endpoint para devolver la lista de solicitudes de amistad
    @GetMapping("/friend-requests")
    public String viewFriendRequests(Model model, @AuthenticationPrincipal UserDetails currentUserDetails) {
        if (currentUserDetails == null) {
            return "redirect:/login";
        }
        User currentUser = userService.findByUsername(currentUserDetails.getUsername());
        if (currentUser == null) {
            return "redirect:/logout"; 
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

        return "friend-requests"; 
    }


}