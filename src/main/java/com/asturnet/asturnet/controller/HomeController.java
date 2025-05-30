package com.asturnet.asturnet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Redirige la raíz "/" a la página de login
    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

    // Muestra la vista de login personalizada
    @GetMapping("/login")
    public String login() {
        return "login";  // Thymeleaf busca login.html en templates
    }
}


