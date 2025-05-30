package com.asturnet.asturnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication; // ¡IMPORTANTE: Esta importación es necesaria!

@SpringBootApplication // ¡IMPORTANTE: Esta anotación es crucial para Spring Boot!
public class AsturNetApplication {

    public static void main(String[] args) { // ¡IMPORTANTE: Este es el punto de entrada de tu aplicación!
        SpringApplication.run(AsturNetApplication.class, args);
    }

}