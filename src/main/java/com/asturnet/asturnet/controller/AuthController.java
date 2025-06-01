package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.service.UserService; 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam; 
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    // Inyección de dependencias del UserService
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Endpoint para devolver la página de login
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Devuelve la plantilla login.html
    }

    // Endpoint para devolver la página de registro
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // Devuelve la plantilla register.html
    }

    // Endpoint para registrarse
    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.registerNewUser(username, email, password);
            // Si el registro es exitoso, redirigimos a la página de login con un mensaje
            redirectAttributes.addFlashAttribute("successMessage", "¡Registro exitoso! Por favor, inicia sesión.");
            return "redirect:/login"; // Redirige a la página de login
        } catch (RuntimeException e) { // Captura la excepción de usuario/email existente
            model.addAttribute("errorMessage", e.getMessage());
            // Si falla, volvemos a la página de registro y mostramos el error
            return "register";
        }
    }
}