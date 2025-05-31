package com.asturnet.asturnet.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // El texto del comentario

    @ManyToOne(fetch = FetchType.LAZY) // Relación Muchos a Uno con User
    @JoinColumn(name = "user_id", nullable = false) // Columna en la tabla 'comments' para el ID del usuario
    private User user; // El usuario que hizo el comentario

    @ManyToOne(fetch = FetchType.LAZY) // Relación Muchos a Uno con Post
    @JoinColumn(name = "post_id", nullable = false) // Columna en la tabla 'comments' para el ID del post
    private Post post; // La publicación a la que pertenece el comentario

    @CreationTimestamp // Fecha de creación automática
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}