package com.asturnet.asturnet.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set; // Para las colecciones de comentarios y likes

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Relación Many-to-One con User
    @JoinColumn(name = "user_id", nullable = false) // Columna user_id en la tabla posts
    private User user; // El usuario que creó el post

    @Column(columnDefinition = "TEXT", nullable = false) // Contenido del post, TEXT y no nulo
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Relaciones de JPA con otras entidades (opcional, puedes añadir más tarde) ---

    /*
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments; // Comentarios en este post

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes; // Likes en este post

    @OneToMany(mappedBy = "reportedPost", cascade = CascadeType.ALL)
    private Set<Report> reports; // Reportes sobre este post
    */
}