package com.asturnet.asturnet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";  // Va a buscar src/main/resources/templates/index.html
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Puedes crear otro template login.html si quieres
    }
}

