package com.asturnet.asturnet.controller; // O el paquete donde quieras poner los controladores de vista

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // Importante: Usamos @Controller para controladores que devuelven vistas
public class HomeController {

    @GetMapping("/") // Mapea la URL raíz "/"
    public String homepage(Model model) {
        model.addAttribute("message", "¡Aquí construiremos nuestra gran red social!");
        return "index"; // Devuelve el nombre de la plantilla (index.html en /templates/)
    }

    // Podrías tener un endpoint para login, por ejemplo
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Asumiendo que tendrás un login.html en /templates/
    }
}