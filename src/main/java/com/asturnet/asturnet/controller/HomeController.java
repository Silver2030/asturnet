package com.asturnet.asturnet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/") // Mapea la URL raíz "/"
    public String homepage(Model model) {
        model.addAttribute("message", "¡Aquí construiremos nuestra gran red social!");
        return "index"; // Devuelve el nombre de la plantilla (index.html en /templates/)
    }

}