package com.asturnet.asturnet.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "TEXT")
    private String bio;

    // Cambiado de photo_url a profile_picture_url para coincidir con la BD
    @Column(name = "profile_picture_url", columnDefinition = "TEXT")
    private String profilePictureUrl;

    // Nuevo campo: full_name (no es nullable en DB)
    @Column(name = "full_name", length = 255) // Según tu imagen, no es NOT NULL
    private String fullName;

    // Columna para el nivel de privacidad (enum)
    @Enumerated(EnumType.STRING)
    @Column(name = "privacy_level", nullable = false, length = 20)
    private PrivacyLevel privacyLevel = PrivacyLevel.PUBLIC; // Valor por defecto

    // Campo is_private (BOOLEAN NOT NULL en DB)
    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false; // Valor por defecto en la entidad para evitar nulls

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ... (tus relaciones OneToMany, etc., si las tienes)
}