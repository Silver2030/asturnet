package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.service.UserService; // Importa tu UserService
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Para añadir atributos al modelo de Thymeleaf
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam; // Para obtener parámetros del formulario
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Para mensajes flash en redirecciones

@Controller
public class AuthController {

    private final UserService userService;

    // Inyección de dependencias del UserService
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // --- Páginas de Login y Registro ---

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Devuelve la plantilla login.html
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // Devuelve la plantilla register.html
    }

    // --- Manejo del Registro ---

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

    // Spring Security maneja el POST de /login automáticamente por defecto.
    // No necesitamos un @PostMapping("/login") explícito aquí a menos que quieras personalizarlo mucho.
    // La configuración en SecurityConfig ya redirige a "/login" para la página de login.
    // Para el POST de login, Spring Security espera que el formulario envíe 'username' y 'password'
    // y los procesará automáticamente.
}