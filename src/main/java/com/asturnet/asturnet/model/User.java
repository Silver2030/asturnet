package com.asturnet.asturnet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;  // Recuerda almacenar hashed

    @Column(nullable = false)
    private String email;

    private String fullName;

    @Column(length = 500)
    private String bio;

    private String profilePictureUrl;

    private boolean isPrivate = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public User() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

