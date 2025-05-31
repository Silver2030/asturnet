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

    @GetMapping("/profile/edit")
    public String showEditProfilePage(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        // Obtenemos el usuario de nuestra base de datos usando el username de Spring Security
        User user = userService.findByUsername(currentUser.getUsername());
        model.addAttribute("user", user); // Añadimos el objeto User al modelo
        // También podríamos añadir un UserProfileUpdateRequest vacío si lo necesitáramos para el formulario
        // model.addAttribute("profileUpdateRequest", new UserProfileUpdateRequest());
        return "edit-profile"; // Devuelve la plantilla edit-profile.html
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@AuthenticationPrincipal UserDetails currentUser,
                                @ModelAttribute UserProfileUpdateRequest request, // Captura los datos del formulario
                                RedirectAttributes redirectAttributes) {
        try {
            // Obtenemos el ID de nuestro usuario para pasarlo al servicio
            User user = userService.findByUsername(currentUser.getUsername());
            userService.updateProfile(user.getId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado exitosamente.");
            return "redirect:/home"; // Redirige a la página de inicio o a la de perfil
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar perfil: " + e.getMessage());
            return "redirect:/profile/edit"; // Vuelve a la página de edición con el error
        }
    }

    // Opcional: Una página para ver el perfil (podría ser el mismo usuario o ver otros)
    @GetMapping("/profile")
    public String viewMyProfile(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        User user = userService.findByUsername(currentUser.getUsername());
        model.addAttribute("user", user);
        return "view-profile"; // Una nueva plantilla para ver el perfil
    }

    // Más tarde, podrías tener @GetMapping("/profile/{username}") para ver perfiles de otros
}