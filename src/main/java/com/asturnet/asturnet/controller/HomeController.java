package com.asturnet.asturnet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Para obtener el usuario autenticado
import org.springframework.security.core.userdetails.UserDetails; // Tipo de usuario de Spring Security

@Controller
public class HomeController {

    @GetMapping("/") // Mapea la URL raíz "/"
    public String welcomePage(Model model) {
        model.addAttribute("message", "¡Bienvenido a AsturNet! Inicia sesión para empezar.");
        return "index"; // Devuelve la plantilla index.html
    }

    @GetMapping("/home") // Nueva ruta para la página de inicio post-login
    public String homePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
        } else {
            model.addAttribute("username", "Invitado"); // En caso de que se acceda sin autenticar (aunque no debería pasar si está protegida)
        }
        return "home"; // Devuelve la plantilla home.html
    }
}