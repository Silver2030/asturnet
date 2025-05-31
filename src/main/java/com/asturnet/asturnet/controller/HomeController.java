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
    
}