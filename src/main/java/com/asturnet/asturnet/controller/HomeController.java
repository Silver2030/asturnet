package com.asturnet.asturnet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.service.UserService;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal; // Para obtener el usuario autenticado
import org.springframework.security.core.userdetails.UserDetails; // Tipo de usuario de Spring Security

@Controller
public class HomeController {

     private final UserService userService;

         public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/") // Mapea la URL raíz "/"
    public String welcomePage(Model model) {
        model.addAttribute("message", "¡Bienvenido a AsturNet! Inicia sesión para empezar.");
        return "index"; // Devuelve la plantilla index.html
    }

        @GetMapping("/search")
    public String searchUsers(@RequestParam(value = "query", required = false) String query, Model model) {
        List<User> searchResults = List.of(); // Inicializa como lista vacía

        if (query != null && !query.trim().isEmpty()) {
            searchResults = userService.searchUsers(query.trim());
            model.addAttribute("query", query); // Para mostrar la query en el input de búsqueda
        }

        model.addAttribute("searchResults", searchResults);
        return "search-results"; // Nombre de tu nueva plantilla Thymeleaf para resultados
    }
    
}