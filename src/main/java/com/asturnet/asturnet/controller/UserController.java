package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.service.UserService;
import com.asturnet.asturnet.dto.UserProfileUpdateRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Endpoint para obtener la edicion el perfil
    @GetMapping("/profile/edit")
    public String showEditProfilePage(@AuthenticationPrincipal UserDetails currentUserDetails, Model model) {
        User user = userService.findByUsername(currentUserDetails.getUsername());
        model.addAttribute("user", user);

        UserProfileUpdateRequest profileUpdateRequest = new UserProfileUpdateRequest();
        profileUpdateRequest.setFullName(user.getFullName());
        profileUpdateRequest.setBio(user.getBio());
        profileUpdateRequest.setProfilePictureUrl(user.getProfilePictureUrl());
        profileUpdateRequest.setIsPrivate(user.getIsPrivate()); 

        model.addAttribute("profileUpdateRequest", profileUpdateRequest);

        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.getAttribute("successMessage"));
        }
        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.getAttribute("errorMessage"));
        }

        return "edit-profile";
    }

    // Endpoint para editar el perfil
    @PostMapping("/profile/edit")
    public String updateProfile(@AuthenticationPrincipal UserDetails currentUserDetails,
                                @ModelAttribute UserProfileUpdateRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(currentUserDetails.getUsername());
            userService.updateProfile(user.getId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado exitosamente.");
            return "redirect:/profile";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar perfil: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }
}